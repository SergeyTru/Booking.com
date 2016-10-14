
package com.booking.hackathon;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONTokener;

public class BookingHotels{

  public static JSONArray forCity(int cityID, int limit) throws IOException {
    try {
      HttpClient client = HttpClientBuilder.create().build();
      HttpGet request = new HttpGet("https://hacker240:6PJfyQFLn4@distribution-xml.booking.com/json/bookings.getHotels?cityId=" + cityID + "&rows=" + limit);
      HttpResponse response = client.execute(request);
      System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
      return new org.json.JSONArray(new JSONTokener(response.getEntity().getContent()));
    }
    catch (MalformedURLException ex) {
      Logger.getLogger(BookingHotels.class.getName()).log(Level.SEVERE, null, ex);
      throw new IllegalStateException();
    }
  }
}
