package edu.umkc.rupee.search.dir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;

import org.postgresql.ds.PGSimpleDataSource;

import edu.umkc.rupee.search.lib.Constants;
import edu.umkc.rupee.search.lib.Db;

public class DirInit {

    public static void init() throws Exception {

        String[] chains = Files.list(Paths.get(Constants.DIR_PATH))
            .map(Path::getFileName)
            .map(Path::toString)
            .filter(f -> f.endsWith(".pdb"))
            .map(f -> f.substring(0, f.length() - ".pdb".length()))
            .sorted()
            .toArray(String[]::new);
    
        PGSimpleDataSource ds = Db.getDataSource();

        Connection conn = ds.getConnection();
        conn.setAutoCommit(true);

        PreparedStatement updt = conn.prepareCall("SELECT insert_dir_chains(?);");
        updt.setArray(1, conn.createArrayOf("VARCHAR", chains));
        updt.execute();

        updt.close();
        conn.close();
    }
}
