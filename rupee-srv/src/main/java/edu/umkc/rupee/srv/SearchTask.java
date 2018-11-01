package edu.umkc.rupee.srv;

import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.umkc.rupee.queue.QueueItem;
import edu.umkc.rupee.queue.SearchQueue;

public class SearchTask implements Runnable {

    private BlockingQueue<QueueItem> requestQueue;

    public SearchTask(BlockingQueue<QueueItem> requestQueue) {
        
        this.requestQueue = requestQueue;
    }

    @Override
    public void run() {

        while(true) {
        
            try {

                QueueItem item = requestQueue.take();
            
                SearchStatus.updateStatus(item.getSearchId(), "processing");

                SearchQueue.search(item);    

                SearchStatus.updateStatus(item.getSearchId(), "complete");

            } catch (Exception e) {
                Logger.getLogger(SearchTask.class.getName()).log(Level.SEVERE, null, e);
            }
        }           
    }
}
