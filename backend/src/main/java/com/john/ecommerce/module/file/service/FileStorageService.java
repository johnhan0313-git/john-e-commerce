package com.john.ecommerce.module.file.service;

import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.config.AppProperties;
import com.john.ecommerce.module.file.dto.FileUploadVO;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final long MAX_BYTES = 8L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final Set<String> ALLOWED_FOLDERS = Set.of(
            "logo", "product", "shop", "banner", "misc"
    );

    private final MinioClient minioClient;
    private final AppProperties appProperties;

    private volatile boolean bucketReady;

    public FileUploadVO upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择文件");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BizException("图片不能超过 8MB");
        }
        String contentType = normalizeContentType(file.getContentType());
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new BizException("仅支持 JPG / PNG / WebP / GIF");
        }
        String dir = normalizeFolder(folder);
        ensureBucket();

        String ext = extensionFor(contentType, file.getOriginalFilename());
        Long tenantId = TenantContext.getTenantId();
        String tenantPart = tenantId != null ? String.valueOf(tenantId) : "0";
        String objectKey = "tenant/" + tenantPart + "/" + dir + "/"
                + UUID.randomUUID().toString().replace("-", "") + ext;

        AppProperties.Minio minio = appProperties.getMinio();
        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minio.getBucket())
                    .object(objectKey)
                    .stream(in, file.getSize(), -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            log.error("MinIO upload failed: {}", e.getMessage());
            throw new BizException("上传失败，请稍后重试");
        }

        FileUploadVO vo = new FileUploadVO();
        vo.setUrl(publicUrl(minio.getBucket(), objectKey));
        vo.setObjectKey(objectKey);
        vo.setContentType(contentType);
        vo.setSize(file.getSize());
        return vo;
    }

    private void ensureBucket() {
        if (bucketReady) return;
        synchronized (this) {
            if (bucketReady) return;
            AppProperties.Minio minio = appProperties.getMinio();
            String bucket = minio.getBucket();
            try {
                boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
                if (!exists) {
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                }
                // Public read so browser can display image URLs directly.
                String policy = """
                        {
                          "Version":"2012-10-17",
                          "Statement":[{
                            "Effect":"Allow",
                            "Principal":{"AWS":["*"]},
                            "Action":["s3:GetObject"],
                            "Resource":["arn:aws:s3:::%s/*"]
                          }]
                        }
                        """.formatted(bucket);
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                        .bucket(bucket)
                        .config(policy)
                        .build());
                bucketReady = true;
            } catch (Exception e) {
                log.error("MinIO bucket init failed: {}", e.getMessage());
                throw new BizException("对象存储未就绪");
            }
        }
    }

    private String publicUrl(String bucket, String objectKey) {
        AppProperties.Minio minio = appProperties.getMinio();
        String base = minio.getPublicBaseUrl();
        if (base == null || base.isBlank()) {
            base = minio.getEndpoint();
        }
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/" + bucket + "/" + objectKey;
    }

    private String normalizeFolder(String folder) {
        String raw = folder == null || folder.isBlank() ? "misc" : folder.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_FOLDERS.contains(raw)) {
            throw new BizException("不支持的上传目录");
        }
        return raw;
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) return "";
        return contentType.split(";")[0].trim().toLowerCase(Locale.ROOT);
    }

    private String extensionFor(String contentType, String originalName) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            case "image/jpeg" -> ".jpg";
            default -> {
                if (originalName != null) {
                    int dot = originalName.lastIndexOf('.');
                    if (dot > 0 && dot < originalName.length() - 1) {
                        yield originalName.substring(dot).toLowerCase(Locale.ROOT);
                    }
                }
                yield ".bin";
            }
        };
    }
}
