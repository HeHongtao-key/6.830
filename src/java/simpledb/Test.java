package simpledb;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Test {
    public static void main(String[] argv) {
        int[][] EXAMPLE_VALUES = new int[][] {
                { 1, 1, 1 },
                { 2, 2, 2 },
                { 3, 4, 4 }
        };

        // Build the input table
        ArrayList<ArrayList<Integer>> table = new ArrayList<ArrayList<Integer>>();
        for (int[] tuple : EXAMPLE_VALUES) {
            ArrayList<Integer> listTuple = new ArrayList<Integer>();
            for (int value : tuple) {
                listTuple.add(value);
            }
            table.add(listTuple);
        }

        // Convert it to a HeapFile and read in the bytes
        File file = new File("some_data_file.dat");
        try {
            System.out.println(file.getAbsolutePath());
            HeapFileEncoder.convert(table, file, BufferPool.getPageSize(), 3);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // construct a 3-column table schema
        Type types[] = new Type[]{Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE};
        String names[] = new String[]{"field0", "field1", "field2"};
        TupleDesc descriptor = new TupleDesc(types, names);

        // create the table, associate it with some_data_file.dat
        // and tell the catalog about the schema of this table.
        HeapFile table1 = new HeapFile(new File("some_data_file.dat"), descriptor);
        Database.getCatalog().addTable(table1, "test");

        // construct the query: we use a simple SeqScan, which spoonfeeds
        // tuples via its iterator.
        TransactionId tid = new TransactionId();
        SeqScan f = new SeqScan(tid, table1.getId());

        try {
            // and run it
            f.open();
            while (f.hasNext()) {
                Tuple tup = f.next();
                System.out.println(tup);
            }
            f.close();
            Database.getBufferPool().transactionComplete(tid);
        } catch (Exception e) {
            System.out.println("Exception : " + e);
        }
    }
}
