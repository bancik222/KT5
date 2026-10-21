package ru.ithub.chess;
import java.nio.file.*;
import java.sql.*;
import java.time.Instant;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
public final class TestResultImporter {
    public static void main(String[] args) throws Exception {
        Path folder = Path.of(args[1]);
        if (!Files.isDirectory(folder)) { System.out.println("No test XML found"); return; }
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + args[0])) {
            c.setAutoCommit(false);
            try (PreparedStatement run = c.prepareStatement(
                    "INSERT INTO test_runs(run_at,suite,total,passed,failed,skipped,duration_ms) VALUES(?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement result = c.prepareStatement(
                    "INSERT INTO test_results(run_id,test_name,status,duration_ms,error_message) VALUES(?,?,?,?,?)")) {
                int total=0, failed=0, skipped=0, duration=0;
                java.util.List<String[]> details = new java.util.ArrayList<>();
                try (var files = Files.list(folder)) {
                    for (Path p : files.filter(x -> x.getFileName().toString().startsWith("TEST-") && x.toString().endsWith(".xml")).toList()) {
                        Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(p.toFile());
                        NodeList nodes = d.getElementsByTagName("testcase");
                        for (int i=0;i<nodes.getLength();i++) {
                            Element t=(Element)nodes.item(i);
                            boolean bad=t.getElementsByTagName("failure").getLength()>0 || t.getElementsByTagName("error").getLength()>0;
                            boolean skip=t.getElementsByTagName("skipped").getLength()>0;
                            String status=bad?"FAILED":skip?"SKIPPED":"PASSED";
                            String message=bad?t.getElementsByTagName("failure").item(0)!=null?
                                ((Element)t.getElementsByTagName("failure").item(0)).getAttribute("message"):
                                ((Element)t.getElementsByTagName("error").item(0)).getAttribute("message"):"";
                            int ms=(int)Math.round(Double.parseDouble(t.getAttribute("time"))*1000);
                            details.add(new String[]{t.getAttribute("name"),status,Integer.toString(ms),message});
                            total++; duration+=ms; if(bad)failed++; if(skip)skipped++;
                        }
                    }
                }
                if(total==0) { System.out.println("No test cases found"); return; }
                run.setString(1,Instant.now().toString());run.setString(2,"database");
                run.setInt(3,total);run.setInt(4,total-failed-skipped);run.setInt(5,failed);
                run.setInt(6,skipped);run.setInt(7,duration);run.executeUpdate();
                ResultSet keys=run.getGeneratedKeys();keys.next();long runId=keys.getLong(1);
                for(String[] item:details) {
                    result.setLong(1,runId);result.setString(2,item[0]);result.setString(3,item[1]);
                    result.setInt(4,Integer.parseInt(item[2]));result.setString(5,item[3]);result.addBatch();
                }
                result.executeBatch();c.commit();
                System.out.println("Saved test run "+runId+" to SQLite: "+total+" total, "+failed+" failed");
            }
        }
    }
}
