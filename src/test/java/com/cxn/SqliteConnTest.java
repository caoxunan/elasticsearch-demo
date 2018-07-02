package com.cxn;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

/**
 * @program: elasticsearch-demo
 * @description: ${description}
 * @author: cxn
 * @create: 2018-06-22 14:37
 * @Version v1.0
 */
public class SqliteConnTest {

    /**
     * 初始化uuid的随机数组
     */
    public static List<String> uuidList;

    @Before
    public void prepareList() {
        uuidList = new ArrayList<>(30000000);

        File uuidFile = new File("/Users/caoxunan/learn-git/elasticsearch-demo/sqlitefile/uuidFile");
        FileReader fileReader = null;
        try {

            fileReader = new FileReader(uuidFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String temp = "";
            int i = 0;
            while ((temp = bufferedReader.readLine()) != null) {
                uuidList.add(temp);
                i++;
                if (i == 20000000) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //@Test
    public void testSqliteConn() {
        Set<String> set = new HashSet<>(32);
        int num = 30;
        while (set.size() < num) {
            Random random = new Random();
            int rand = random.nextInt(20000000);
            set.add(uuidList.get(rand));
        }
        StringBuilder stringBuilder = new StringBuilder("");
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            stringBuilder.append("'" + next + "',");
        }

        stringBuilder.setLength(stringBuilder.length() - 1);
        String[] params = set.toArray(new String[0]);
        System.out.println(stringBuilder.toString());
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:/Users/caoxunan/vData/sqliteDB/imagesearch.db");
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            //String sql = "insert into imageurl values('id007','http:///image.zhihuiya.com/search/id007');";
            //ResultSet rs = stmt.executeQuery(sql);
            long startTime = System.currentTimeMillis();
            //ResultSet rs = stmt.executeQuery( "SELECT id,url FROM imageurl limit 40000000,15;" );
            //ResultSet rs = stmt.executeQuery( "SELECT id,url FROM imageurl limit 40000000,15;" );
            ResultSet rs = stmt.executeQuery("SELECT id,url FROM imageurl WHERE id in ("+stringBuilder.toString()+");");
            long endTime = System.currentTimeMillis();
            System.out.println("sqlite 执行sql花费：" + (endTime - startTime) + "ms");
            int hits = 0;
            while (rs.next()) {
                hits++;
                //String  id = rs.getString("id");
                //String  url = rs.getString("url");
                //System.out.println( "id = " + id );
                //System.out.println( "url = " + url );
            }
            System.out.println("hits:" + hits + "次");
            rs.close();
            stmt.close();
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        System.out.println("Operation done successfully");
    }
}
