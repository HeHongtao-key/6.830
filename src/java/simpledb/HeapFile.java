package simpledb;

import java.io.*;
import java.util.*;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see simpledb.HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {

    /**
     * Constructs a heap file backed by the specified file.
     * 
     * @param f
     *            the file that stores the on-disk backing store for this heap
     *            file.
     */
    private final File file;
    private final TupleDesc td;

    public HeapFile(File f, TupleDesc td) {
        // some code goes here
        this.file = f;
        this.td = td;
    }

    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // some code goes here
        return file;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere to ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     * 
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        // some code goes here
//        throw new UnsupportedOperationException("implement this");
        return file.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
//        throw new UnsupportedOperationException("implement this");
        return td;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        // some code goes here
//        try{
//            RandomAccessFile raf = new RandomAccessFile(file, "r");
//            int pageNo = pid.getPageNumber();
//            int pageSize = BufferPool.getPageSize();
//            byte[] b = new byte[pageSize];
//            raf.read(b, pageSize * pageNo, pageSize);
//            return new HeapPage((HeapPageId)pid, b);
//
//
//        }catch (FileNotFoundException e){
//            e.printStackTrace();
//        }catch (IOException e){
//            e.printStackTrace();
//        }

        int tableId = pid.getTableId();
        int pgNo = pid.getPageNumber();
        int pageSize = BufferPool.getPageSize();
        RandomAccessFile f = null;
        try {
            f = new RandomAccessFile(file,"r");
            //边界判断
            if((pgNo + 1) * pageSize > f.length()){
                throw new IllegalArgumentException(String.format("table %d page %d is invalid", tableId, pgNo));
            }
            byte[] bytes = new byte[pageSize];
            f.seek(pgNo*BufferPool.getPageSize());
            int read = f.read(bytes, 0, BufferPool.getPageSize());
            if(read != pageSize){
                throw new IllegalArgumentException(String.format("table %d page %d is invalid", tableId, pgNo));
            }
            return new HeapPage((HeapPageId) pid,bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(f != null){
                    f.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new IllegalArgumentException(String.format("table %d page %d is invalid", tableId, pgNo));
//        return null;
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
        // not necessary for lab1
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        // some code goes here
        return (int) file.length() / BufferPool.getPageSize();
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        // some code goes here
        return new HeapFileIterator(this, tid);
    }

    private class HeapFileIterator implements DbFileIterator{
        private HeapFile heapFile;
        private TransactionId transactionId;
        private Iterator<Tuple> iterator;
        private int currPage;

        public HeapFileIterator(HeapFile heapFile, TransactionId transactionId){
            this.heapFile = heapFile;
            this.transactionId = transactionId;
        }

        private Iterator<Tuple> getIterator(int pageNumber) throws TransactionAbortedException, DbException {
            if(pageNumber >= 0 && pageNumber < heapFile.numPages()){
                HeapPageId pageId = new HeapPageId(heapFile.getId(), pageNumber);
                HeapPage page = (HeapPage) Database.getBufferPool().getPage(transactionId, pageId, Permissions.READ_ONLY);
                return page.iterator();
            }else{
                throw new DbException(String.format("problems opening/accessing the database pageNo %d ", pageNumber));
            }
        }

        @Override
        public void open() throws DbException, TransactionAbortedException {
            currPage = 0;
            iterator = getIterator(currPage);
        }

        @Override
        public boolean hasNext() throws DbException, TransactionAbortedException {
            if(iterator == null){
                return false;
            }else if(iterator.hasNext()){
                return true;
            }else{
                // get next iterator
                currPage++;
                if(currPage >= heapFile.numPages()){
                    return false;
                }else{
                    iterator = getIterator(currPage);
                    return hasNext();
                }
            }
        }

        @Override
        public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
            if(iterator == null || !iterator.hasNext()){
                throw new NoSuchElementException();
            }
            return iterator.next();
        }

        @Override
        public void rewind() throws DbException, TransactionAbortedException {
            close();
            open();
        }

        @Override
        public void close() {
            iterator = null;
        }

    }

}

