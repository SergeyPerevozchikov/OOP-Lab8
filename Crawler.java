import java.util.*;
import java.io.*;
import java.net.*;

public class Crawler 
{
    static int numThreads;
    static int timeOut = 1000;
    public static void main(String[] args) 
	{

        URLDepthPair currentDepthPair;
        ArrayList<String> seenURLs = new ArrayList<String>();
        URLPool pool;
		try
		{
			currentDepthPair = new URLDepthPair(args[0]);
			seenURLs.add(currentDepthPair.getURL());
			pool = new URLPool(Integer.parseInt(args[1]));
			pool.put(currentDepthPair);
			numThreads = Integer.parseInt(args[2]);
		}
		catch(Exception exc)
		{
			System.out.println("usage: java Crawler <URL> <depth> <count of threads>");
			return;
		}
        

        int activeAmount = Thread.activeCount();

        while (pool.getWaitThreads() != numThreads)
		{

            if (Thread.activeCount() - activeAmount < numThreads)
			{
                CrawlerTask crawler = new CrawlerTask(pool);
                new Thread(crawler).start();
            }
            else {
                try 
				{
                    Thread.sleep(100);
                }
                catch (InterruptedException ie)
				{
                    System.out.println("InterruptedException");
                }

            }
        }
        for(String s : pool.seenURLs)
		{
            System.out.println(s);
        }

        System.out.println(pool.seenURLs.size());
        System.exit(0);

    }

    public static LinkedList<String> getAllLinks(URLDepthPair myDepthPair) 
	{

        LinkedList<String> URLs = new LinkedList<String>();
        Socket socket1;

        try 
		{
            socket1 = new Socket(myDepthPair.getWebHost(), 80);
        }
        catch (UnknownHostException e)
		{
            System.err.println("UnknownHostException: " + e.getMessage());
            return URLs;
        }
        catch (IOException ex) 
		{
            return URLs;
        }

        try 
		{
            socket1.setSoTimeout(timeOut);
        }
        catch (SocketException exc) 
		{
            System.err.println("SocketException: " + exc.getMessage());
            return URLs;
        }

        String docPath = myDepthPair.getDocPath();
        String webHost = myDepthPair.getWebHost();

        OutputStream outStream;

        try 
		{
            outStream = socket1.getOutputStream();
        }
        catch (IOException e)
		{
            return URLs;
        }

        PrintWriter printWriter = new PrintWriter(outStream, true);
        printWriter.println("GET " + docPath + " HTTP/1.1");
        printWriter.println("Host: " + webHost);
        printWriter.println("Connection: close");
        printWriter.println();

        InputStream inStream;
        try 
		{
            inStream = socket1.getInputStream();
        }
        catch (IOException ioExc)
		{
            System.err.println("IOException: " + ioExc.getMessage());
            return URLs;
        }
        InputStreamReader inStreamReader = new InputStreamReader(inStream);
        BufferedReader BuffReader = new BufferedReader(inStreamReader);

        while (true) 
		{
            String line;
            try 
			{
                line = BuffReader.readLine();
            }
            catch (IOException e)
			{
                return URLs;
            }
            if (line == null)
                break;
            int beginIndex = 0;
            int endIndex = 0;
            int index = 0;

            while (true) 
			{
                String START_URL = "a href=\"";
                String END_URL = "\"";

                index = line.indexOf(START_URL, index);
                if (index == -1)
                    break;
                index += START_URL.length();
                beginIndex = index;

                endIndex = line.indexOf(END_URL, index);
                index = endIndex;

                try 
				{
                    String newLink = line.substring(beginIndex, endIndex);
                    if(URLs.contains(newLink))
                        continue;

                    if(newLink.startsWith("http")) {
                        URLs.add(newLink);
                    }
					else if(!newLink.startsWith("tel")) 
					{
                        if(newLink.startsWith("/"))
                            URLs.add("http://"+webHost+""+newLink);
                        else
                            URLs.add("http://"+webHost+"/"+newLink);
                    }

                }
				catch(Exception exception) 
				{
                    break;
                }

            }

        }
        return URLs;
    }
}