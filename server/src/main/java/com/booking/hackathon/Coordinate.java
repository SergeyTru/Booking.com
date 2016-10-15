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
    double coef = Math.PI / 180;
    int EARTH_RADIUS = 6371;//in km
    return Math.acos(Math.sin(coef*lat) * Math.sin(coef*other.lat)
        + Math.cos(coef*lat) * Math.cos(coef*other.lat) * Math.cos(coef*other.lon - coef*lon)) * EARTH_RADIUS;
  }

  double avgDistanceTo(List<Coordinate> till)
  {
    return till.stream().mapToDouble(x -> distanceTo(x)).average().orElse(0);
  }

  @Override
  public int hashCode() {
    return (int) (Double.doubleToLongBits(this.lon) ^ Double.doubleToLongBits(this.lon));
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final Coordinate other = (Coordinate) obj;
    if (Double.doubleToLongBits(this.lon) != Double.doubleToLongBits(other.lon))
      return false;
    if (Double.doubleToLongBits(this.lat) != Double.doubleToLongBits(other.lat))
      return false;
    return true;
  }
}
