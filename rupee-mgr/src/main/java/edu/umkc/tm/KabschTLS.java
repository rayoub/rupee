package edu.umkc.tm;

public class KabschTLS {

     private static final ThreadLocal<Kabsch> k =
         new ThreadLocal<Kabsch>() {
             @Override protected Kabsch initialValue() {
                 return new Kabsch();
         }
     };

     public static Kabsch get() {
         return k.get();
     }
 }
