package edu.umkc.rupee.srv;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgresql.ds.PGSimpleDataSource;

import edu.umkc.rupee.lib.Db;
import edu.umkc.rupee.queue.QueueItem;

public class Main {

	private static final int NUM_THREADS = 4;
    private static final int MAX_CAPACITY = 1000;
    private static final int SLEEP_TIME = 60000; // 1 minute

    public static void main(String[] args) {

        PGSimpleDataSource ds = Db.getDataSource();

		try {
            
            // *** create blocking queue and start search threads
            
            BlockingQueue<QueueItem> requestQueue = new LinkedBlockingQueue<QueueItem>(MAX_CAPACITY);
			
			List<Thread> searchThreads = new ArrayList<Thread>();
			for (int i = 0; i < NUM_THREADS; i++) {
				Runnable searchTask = new SearchTask(requestQueue);
				Thread searchThread = new Thread(searchTask);
				searchThreads.add(searchThread);
		        searchThread.start();
			}

            // *** periodically check queue
            
            while(true) {

                Connection conn = ds.getConnection();
                conn.setAutoCommit(true);

                // expire entries 
                
                Instant instant = Instant.now().minusSeconds(60 * 60 * 24); // 24 hours back

                PreparedStatement exp = conn.prepareCall("SELECT expire_search_queue(?);");
                exp.setTimestamp(1, Timestamp.from(instant));

                exp.execute();

                // service queue

                PreparedStatement stmt = conn.prepareCall("SELECT * from search_queue WHERE status = 'pending' ORDER BY inserted_on DESC;");

                ResultSet rs = stmt.executeQuery();
                while(rs.next()) {

                    QueueItem item = new QueueItem(rs);

                    requestQueue.add(item);

                    SearchStatus.updateStatus(item.getSearchId(), "queued");
                }
      
                Thread.sleep(SLEEP_TIME);
            }
        
	    } catch (InterruptedException e) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
		} catch (SQLException e) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}


