package com.kiots;


import org.jsoup.Jsoup;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
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
        scrapeTopic("wiki/Google");
    }

    public static void scrapeTopic(String url){
        String html=getUrl("https://en.wikipedia.org/"+url);
        Document doc = Jsoup.parse(html);

        Element mainContent = doc.select("#content").first();

        if(mainContent != null){
            Elements links = mainContent.select("a[href]");

        if(links.size()==0){
            System.out.println("no links found at " + url + ". Going back to Google...");
            scrapeTopic("wiki/Google");

        }else{

            // for(Element link: links){
            //     String text = link.text();
            //     String href = link.attr("href");
            //     System.out.println("text: " + text + ", ");
            //     System.out.println("href: " + href);
            // }  

            System.out.println("-------------------------------------------");
            int random = generator.nextInt(links.size());
            System.out.println("random link is: " + links.get(random).text() + ", url is: " + links.get(random).attr("href"));

            String summary = getLinkSummary("https://en.wikipedia.org/" + links.get(random).attr("href"));
            System.out.println("Summary: " + summary);

           

            try{
                Thread.sleep(4000);

            }catch(InterruptedException e){
                e.printStackTrace();
                System.err.println("err during sleep");
            }

            scrapeTopic(links.get(random).attr("href"));
            // try(BufferedWriter writer = new BufferedWriter(new FileWriter("results.txt"))){
            //     for(Element link: links){
            //         String text = link.text();
            //         String href = link.attr("href");
            //         writer.write("text: " + text + ", ");
            //         writer.write("href: " + href);
            //         writer.write("\n");
            //     }
            // }catch(IOException e){
            //     e.printStackTrace();
            //     System.out.println("error writing to file");
            // }
            
        }
        }
        

        // int randomLink = generator.nextInt(links.size());
        // System.out.println("Random link is: " + links.get(randomLink).toString() + ", at url: " + links.get(randomLink).attr("href"));
    }

    public static String getLinkSummary(String url) {
        String html = getUrl(url);
        Document doc = Jsoup.parse(html);
    
        Element summaryElement = doc.select("#mw-content-text p").first(); // Adjust the selector as needed
        if (summaryElement != null) {
            StringBuilder summary = new StringBuilder();
            for (TextNode textNode : summaryElement.textNodes()) {
                summary.append(textNode.text()).append(" ");
            }
            return summary.toString().trim();
        }
        return "No summary found.";
    }

    public static String getUrl(String url){
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
                scrapeTopic("wiki/Google");
            }

        }catch(IOException e){
            System.out.println("error connecting to url");
            return "";
        }
        return "";
    }
}
