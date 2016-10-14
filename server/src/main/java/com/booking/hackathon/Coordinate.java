package com.booking.hackathon;

import java.util.List;

public class Coordinate {
  final double lon;
  final double lat;

  public Coordinate(double longtitude, double latitude) {
    this.lon = longtitude;
    this.lat = latitude;
  }

  public double distanceTo(Coordinate other)
  {
    int EARTH_RADIUS = 6371;//in km
    return Math.acos(Math.sin(lat) * Math.sin(other.lat)
        + Math.cos(lat) * Math.cos(other.lat) * Math.cos(other.lon - lon)) * EARTH_RADIUS;
  }

  double avgDistanceTo(List<Coordinate> till)
  {
    return till.stream().mapToDouble(x -> distanceTo(x)).average().orElse(0);
  }
}
