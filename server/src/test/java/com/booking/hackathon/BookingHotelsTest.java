package com.booking.hackathon;

import java.io.IOException;
import org.json.JSONArray;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author SergeyT
 */
public class BookingHotelsTest {

  @Test
  public void testForCity() throws IOException {
    JSONArray hotels = BookingHotels.hotelsForCity(-2140479, 3);
    assertEquals(3, hotels.length());
    for (int i = 0; i < hotels.length(); i++) {
      assertTrue(hotels.getJSONObject(i).has("location"));
      assertTrue(hotels.getJSONObject(i).has("name"));
    }
  }

}
