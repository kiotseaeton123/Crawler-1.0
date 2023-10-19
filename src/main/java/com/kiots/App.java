package com.kiots;

import org.jsoup.Jsoup;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import org.jsoup.select.Elements;
import java.net.*;
import java.io.*;
import java.util.Random;

public class App 
{
    private static Random generator = new Random();

    public static void main( String[] args )
    {

    }

    public static void start(){
        for(int i = 5; i > 0; i--){
            try{
                scrapeTopic("wiki/Google");

            }catch(LinkNotFoundException e){
                System.out.println("link not found, trying again..." + i);
            }

        }
    }

    public static void scrapeTopic(String url) throws LinkNotFoundException{
        String html=getUrl("https://en.wikipedia.org/"+url);
        Document doc = Jsoup.parse(html);

        Element mainContent = doc.select("#bodyContent").first();

        // System.out.println(mainContent.toString());

        if(mainContent != null){

            Elements links = mainContent.select("a[href]");

            if(links.size()==0){
                throw new LinkNotFoundException("no links on page, or page is malformed...");

            }else{
                System.out.println("Links found: " + links.size());

                //4 second pause
                try{
                    Thread.sleep(4000);

                }catch(InterruptedException e){
                    e.printStackTrace();
                    System.err.println("err during sleep");
                }

                //print links
                for(Element link: links){
                    String text = link.text();
                    String href = link.attr("href");
                    System.out.println("text: " + text + ", ");
                    System.out.println("href: " + href);
                }  

                System.out.println("-------------------------------------------");
                //crawl random link
                int random = generator.nextInt(links.size());
                System.out.println("random link is: " + links.get(random).text() + ", url is: " + links.get(random).attr("href"));
                //link summary
                String summary = getLinkSummary("https://en.wikipedia.org/" + links.get(random).attr("href"));
                System.out.println("Summary: " + summary);


                //4 second pause
                try{
                    Thread.sleep(4000);

                }catch(InterruptedException e){
                    e.printStackTrace();
                    System.err.println("err during sleep");
                }

                scrapeTopic(links.get(random).attr("href"));
                
            }
        }
    }

    public static String getLinkSummary(String url) throws LinkNotFoundException{
        String html = getUrl(url);
        Document doc = Jsoup.parse(html);
    
        Element summaryElement = doc.select(".mw-parser-output p").first(); // Adjust the selector as needed
        if (summaryElement != null) {
            StringBuilder summary = new StringBuilder();
            for (TextNode textNode : summaryElement.textNodes()) {
                summary.append(textNode.text()).append(" ");
            }
            return summary.toString().trim();
        }
        return "No summary found.";
    }

    public static String getUrl(String url) throws LinkNotFoundException{
        URL urlObject=null;
        try{
            urlObject=new URL(url);

        }catch(MalformedURLException e){
            System.out.println("url is malformed"); 
            return "";
        }

        try{
            HttpURLConnection connection=(HttpURLConnection) urlObject.openConnection();
            connection.setRequestMethod("GET");

            int responseCode=connection.getResponseCode();
            System.out.println(responseCode);

            if(responseCode==HttpURLConnection.HTTP_OK){
                BufferedReader input=new BufferedReader(new InputStreamReader(connection.getInputStream()));

                StringBuilder output=new StringBuilder();
                String line;
                while((line = input.readLine())!= null){
                    output.append(line);
                }
                input.close();

                return output.toString();
            }else if(responseCode ==HttpURLConnection.HTTP_MOVED_PERM || responseCode==HttpURLConnection.HTTP_MOVED_TEMP){
                String newUrl=connection.getHeaderField("Location");
                if(newUrl != null){
                    return getUrl(newUrl);
                }
            }else if(responseCode == HttpURLConnection.HTTP_NOT_FOUND){
                throw new LinkNotFoundException("no links on page, or page is malformed...");

            }

        }catch(IOException e){
            System.out.println("error connecting to url");
            return "";
        }
        return "";
    }
}
