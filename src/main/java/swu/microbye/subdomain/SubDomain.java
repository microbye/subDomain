package swu.microbye.subdomain;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import swu.microbye.subdomain.PathUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.pipeline.FilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.PriorityScheduler;
import us.codecraft.webmagic.scheduler.QueueScheduler;
import java.net.InetAddress;
/**
 * 输入一个顶级域名
 * 例如：
 *
 * java -jar **.jar qq.com  1000
 * 表示获取主域名为qq.com的所有子域名。获取时间按为1000s
 * 时间越长，理论上能够拿到的子域名就越多
 *
 * 这个工具可以用于域名管理时不清楚有多少个二级三级域名了，
 * 用来一次性比对一下二级域名的情况
 *
 *
 * */

public class SubDomain implements PageProcessor{

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private Site site = Site.me()
            .setRetryTimes(3).setUseGzip(true).setCharset("UTF-8")
			.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 " +
					"(KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36");
	
	private int START_DEPTH = 0;
	private boolean startPage = true;
	private int linkNum;
	private ArrayList<String> hosts = new ArrayList<String>();
	private String domain;
	private long sysDate ;
	private long runTime ;
	public SubDomain(String domain){
		super();
		this.runTime = 600;
		this.domain = domain;	
		this.sysDate = System.currentTimeMillis();
	}
	
	public SubDomain(String domain,long runTime){
		
		super();
		if(runTime >= 20){
			this.runTime = runTime;
		}
		else{
			this.runTime = 600;
		}
		this.domain = domain;	
		this.sysDate = System.currentTimeMillis();
	}

	public Logger getLogger() {
		return logger;
	}

	public void process(Page page) {
		
    	if(page.getUrl().toString().equals("http://"+domain)){
    		startPage = true;
    	}
    	else{
    		startPage = false;
    	}
    	List<String> links = page.getHtml().links().all();
    	for (String url : links) {
    		Request request = new Request();
    	    request.setUrl(url);
    	    Map<String, Object> extras = new HashMap<String, Object>();
    	    if (startPage) {
    	    		extras.put("_level", START_DEPTH + 1);
    	    } 
    	    else {
    	    	//获取上层页面的深度再加一就是这个URL的深度
    	    	extras.put("_level", (Integer) page.getRequest().getExtra("_level") + 1);	
    	    }
    	    
    	    /*限制爬虫深度为5*/
    	    if((Integer)extras.get("_level")>5){
    	    	System.out.println("层数大于5了");
    	    }
    	    else{
    	    	try{  
        			Pattern p = Pattern.compile("[^//]*?\\.(com|cn|net|org|biz|info|cc|tv)", Pattern.CASE_INSENSITIVE);  
        			Matcher matcher;
        			matcher = p.matcher(url);  
            	    matcher.find();  
        	        if(matcher.group().contains("."+domain)){
        	        	boolean is = false;
             	        for(String li: hosts){
             	        	
             	        	if(li.equals(matcher.group())){
             	        		is = true;
             	        	}
             	        }
             	        if (!is){
             	        	hosts.add(matcher.group());
             	        	long interval = System.currentTimeMillis()-this.sysDate;
	                        logger.info(hosts.get(hosts.size()-1)+"\t"+
			                        InetAddress.getByName(hosts.get(hosts.size()-1)).getHostAddress()
			                        );

	                        System.out.println(hosts.size()+"\t"+hosts.get(hosts.size()-1)+"\t"+
			                        InetAddress.getByName(hosts.get(hosts.size()-1)).getHostAddress()
	                        +"\t"+interval/1000+"s");
             	        	if(interval/1000>=this.runTime){
             	        		System.exit(0);
             	        	}
             	        }
        	        	linkNum++;
        	        	List<String> allowPostfix = new ArrayList<String>();
        	        	allowPostfix.add(".html");
        	        	allowPostfix.add(".php");
        	        	allowPostfix.add(".htm");
        	        	allowPostfix.add(".xhtml");
        	        	allowPostfix.add(".json");
        	        	allowPostfix.add(".xml");
        	        	allowPostfix.add(".jsp");
        	        	allowPostfix.add(".com");
        	        	allowPostfix.add(".cn");
        	        	allowPostfix.add(".org");
        	        	boolean yesorno;
        	        	yesorno = PathUtils.allowExtension(url, allowPostfix);
        	        	if(yesorno){ 
        	        		request.setExtras(extras);
                    	    page.addTargetRequest(request);
        	        	}
        	        	else{
        	        	}
        	        }
        	        else{

        	        }
        	    }catch(Exception ex){  
        	          //  ex.printStackTrace();  
        	    }  
    	    	
    	    }   
    	} 	
    }

    public Site getSite() {
        return site;
    }
    
    public static boolean isNumeric(String str){
    	for (int i = 0; i < str.length(); i++){
    		if (!Character.isDigit(str.charAt(i))){
    			return false;
    		}
    	}
    	return true;
    }
    
    public static void main(String[] args) throws FileNotFoundException {
		InputStream is = SubDomain.class.getResourceAsStream("/log4j.properties");
		System.out.println(is);
	    PropertyConfigurator.configure(is);
    	final Logger logger = LoggerFactory.getLogger(SubDomain.class.getName());
    	if(args.length == 0){
		    System.out.println("请传入域名");
    		System.exit(1);
    	}
    	String domain = args[0];
    	long limitTime = 0;
    	if(args.length == 1){
    		limitTime = 600;
    	}
    	else{
    		if(isNumeric(args[1])){
    			limitTime= Integer.parseInt(args[1]);
			    System.out.println("开始获取");
    		}
    		else{
			    System.out.println("参数"+args[1]+"非法,"+"请传入正确参数");
    			System.exit(1);
    		}
    	}
    	SubDomain processor = new SubDomain(domain,limitTime);
    	Spider spider = Spider.create(processor);
    	spider.addUrl("http://"+domain).setScheduler(new QueueScheduler()).addPipeline(new subDomainPipeline());
    	spider.start();
    }
}
