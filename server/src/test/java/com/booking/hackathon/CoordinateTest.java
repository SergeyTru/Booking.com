package com.booking.hackathon;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author SergeyT
 */
public class CoordinateTest {

  public CoordinateTest() {
  }

  @Test
  public void testDistance() {
    List<Coordinate> coordinates = Arrays.asList(new Coordinate(40.05, 20.03),
      new Coordinate(40.07, 20.00), new Coordinate(40.09, 19.95));
    assertEquals(271.15, new Coordinate(40, 20).avgDistanceTo(coordinates), 0.1);
  }

}
