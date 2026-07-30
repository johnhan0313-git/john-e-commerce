package com.john.ecommerce.support;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

/**
 * Loads project {@code scripts/sql/V00x} migrations into a fresh database (Testcontainers).
 */
public final class SqlScriptLoader {

    private static final List<String> SCRIPTS = List.of(
            "V001__init_tenant.sql",
            "V002__init_user.sql",
            "V003__init_product.sql",
            "V004__init_trade.sql",
            "V005__init_activity.sql",
            "V006__init_merchant.sql",
            "V007__init_fulfillment.sql",
            "V008__init_payment.sql",
            "V009__init_content.sql",
            "V010__seed_dev_admin.sql",
            "V011__seed_dev_payment.sql",
            "V012__align_module_def.sql",
            "V013__cart_selected.sql",
            "V014__order_cancel_by.sql"
    );

    private SqlScriptLoader() {}

    public static void loadAll(String jdbcUrl, String username, String password) throws Exception {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setUrl(jdbcUrl);
        ds.setUsername(username);
        ds.setPassword(password);
        loadAll(ds);
    }

    public static void loadAll(DataSource dataSource) throws Exception {
        Path dir = resolveSqlDir();
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement()) {
            for (String name : SCRIPTS) {
                Path file = dir.resolve(name);
                if (!Files.isRegularFile(file)) {
                    throw new IllegalStateException("Missing SQL script: " + file.toAbsolutePath());
                }
                // Execute whole file (supports PG DO $$ blocks); avoid ScriptUtils ';' splitting
                st.execute(Files.readString(file));
            }
        }
    }

    static Path resolveSqlDir() {
        Path cwd = Path.of("").toAbsolutePath();
        Path candidate = cwd.resolve("scripts/sql");
        if (Files.isDirectory(candidate)) {
            return candidate;
        }
        candidate = cwd.resolve("../scripts/sql");
        if (Files.isDirectory(candidate)) {
            return candidate.normalize();
        }
        throw new IllegalStateException(
                "Cannot find scripts/sql from cwd=" + cwd + " (run mvn from repo root or backend/)");
    }
}
