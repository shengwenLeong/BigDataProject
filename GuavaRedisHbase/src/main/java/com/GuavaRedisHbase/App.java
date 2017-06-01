package com.GuavaRedisHbase;

import java.util.concurrent.TimeUnit;  
import java.util.concurrent.ExecutionException;
/**
 * Hello world!
 *
 */
public class App 
{
    //private static Guava guava = new Guava();
    
    public static void main(String[] args ) throws Exception
    {
        Guava guava = new Guava();
        System.out.println( "Hello World!" );
        System.out.println(guava.get("man"));
        UserService us = new UserService();  
        for(int i=0;i<20;i++)  
        {  
            System.out.println(us.getUserName("1001"));  
            TimeUnit.SECONDS.sleep(1);  
        }  
    } 
}
