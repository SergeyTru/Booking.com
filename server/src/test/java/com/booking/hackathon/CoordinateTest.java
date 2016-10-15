package com.booking.hackathon;

import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class CoordinateTest {

  public CoordinateTest() {
  }

  @Test
  public void testDistance() {
    List<Coordinate> coordinates = Arrays.asList(new Coordinate(40.05, 20.03),
      new Coordinate(40.07, 20.00), new Coordinate(40.09, 19.95));
    assertEquals(8.148, new Coordinate(40, 20).avgDistanceTo(coordinates), 0.1);
  }

  @Test
  public void testDistanceExact() {
    assertEquals(2, new Coordinate(17.387111, 40.454988).distanceTo(new Coordinate(17.409176, 40.448332)), 0.1);
  }
}