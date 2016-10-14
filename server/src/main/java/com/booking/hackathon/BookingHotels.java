package com.booking.hackathon;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class BookingHotels{

  private static final HttpClient client = HttpClientBuilder.create().build();

  public static JSONArray hotelsForCity(int cityID, int limit) throws IOException {
    String requestURL = "/bookings.getHotels?cityId=" + cityID;
    if (limit > 0)
      requestURL += "&rows=" + limit;

    try (InputStream stream = streamForUrl(requestURL))
    {
      return new org.json.JSONArray(new JSONTokener(stream));
    }
  }

  public static HashMap<String, String> pictutesForHotels(String hotels) throws IOException {
    final String requestURL = "/bookings.getHotelDescriptionPhotos?hotel_ids=" + hotels;

    try (InputStream stream = streamForUrl(requestURL))
    {
      HashMap<String, String> hotelToPicture = new HashMap<>();
      final JSONArray allPictures = new org.json.JSONArray(new JSONTokener(stream));
      for (int i = 0; i < allPictures.length(); ++i) {
        JSONObject pict = allPictures.getJSONObject(i);
        hotelToPicture.putIfAbsent(pict.optString("hotel_id"), pict.optString("url_max300"));
      }
      return hotelToPicture;
    }
  }

  private static InputStream streamForUrl(final String requestURL) throws UnsupportedOperationException, IOException {
    File cacheFile = new File("cache/" + String.valueOf(requestURL.replaceAll("[?./=&]", "_")) + ".txt");
    if (!cacheFile.exists())
    {
      System.out.println("Create file: " + cacheFile.getCanonicalPath());
      cacheFile.getParentFile().mkdirs();
      final String url = "https://hacker240:6PJfyQFLn4@distribution-xml.booking.com/json" + requestURL;
      HttpResponse response = client.execute(new HttpGet(url));
      if (response.getStatusLine().getStatusCode() != 200)
      {
        System.out.println("Booking response code: " + response.getStatusLine().getStatusCode());
        return null;
      }
      try (InputStream strm = response.getEntity().getContent())
      {
        Files.copy(strm, cacheFile.toPath());
      }
    }
    return new FileInputStream(cacheFile);
  }
}
