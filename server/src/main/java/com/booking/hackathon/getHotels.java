package com.booking.hackathon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet(name = "getHotels", urlPatterns = {"/getHotels"})
public class getHotels extends HttpServlet {

  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("text/json;charset=UTF-8");
    response.addHeader("Access-Control-Allow-Origin", "*");
    response.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
    try (PrintWriter out = response.getWriter()) {
      final String postData = getPostData(request);
      List<Coordinate> placesCoordinates;
      double maxWalkDistance = 1; //km
      if (!postData.trim().isEmpty())
      {
        JSONObject obj = new org.json.JSONObject(postData);
        JSONArray places = obj.getJSONArray("places");
        JSONObject params = obj.optJSONObject("parameters");
        if (params != null)
          maxWalkDistance = params.optDouble("maxWalkDistance", maxWalkDistance);
        placesCoordinates = parsePlaces(places);
      }
      else
        placesCoordinates = Arrays.asList(new Landmark("test", 4.8730, 52.32));
      JSONArray hotels = HotelsSorter.sortByClusters(placesCoordinates, BookingHotels.hotelsForCity(-2140479, 0), maxWalkDistance);
      addPicturesToHotels(hotels);
      addLandmarksToHotels(hotels, placesCoordinates);
      JSONObject result = new JSONObject();
      result.put("hotels", hotels);
      out.println(result.toString());
    }
  }

  // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
  /**
   * Handles the HTTP <code>GET</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Handles the HTTP <code>POST</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Returns a short description of the servlet.
   *
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo() {
    return "Hotels by places";
  }// </editor-fold>

  private String getPostData(HttpServletRequest request) throws IOException {
    StringBuilder sb = new StringBuilder();
    String line;
    BufferedReader reader = request.getReader();
    while ((line = reader.readLine()) != null)
      sb.append(line);
    return sb.toString();
  }

  private List<Coordinate> parsePlaces(JSONArray places) {
    List<Coordinate> result = new ArrayList<>(places.length());
    for (int i = 0; i < places.length(); ++i) {
      JSONObject place = places.getJSONObject(i);
      result.add(new Landmark(place.optString("title"), place.optDouble("longtitude"), place.optDouble("latitude")));
    }
    return result;
  }

  private void addPicturesToHotels(JSONArray hotels) throws IOException {
    StringJoiner sj = new StringJoiner(",");
    for (int i = 0; i < hotels.length(); ++i)
      sj.add(hotels.getJSONObject(i).optString("hotel_id"));
    HashMap<String, String> picts = BookingHotels.pictutesForHotels(sj.toString());
    for (int i = 0; i < hotels.length(); ++i) {
      JSONObject obj = hotels.getJSONObject(i);
      String id = obj.optString("hotel_id");
      if (picts.containsKey(id))
        obj.put("picture", picts.get(id));
    }
  }

  private void addLandmarksToHotels(JSONArray hotels, List<Coordinate> landmarks) {
    for (int i = 0; i < hotels.length(); ++i) {
      JSONObject hotel = hotels.getJSONObject(i);
      JSONObject location = hotel.getJSONObject("location");
      Coordinate coord = new Coordinate(location.getDouble("longitude"), location.getDouble("latitude"));
      JSONArray distances = new JSONArray();
      for (Coordinate landmark: landmarks)
      {
        JSONObject inner = new JSONObject();
        inner.put("title", ((Landmark)landmark).title);
        inner.put("distance", coord.distanceTo(landmark));
        distances.put(inner);
      }
      hotel.put("distance_to_landmark", distances);
    }
  }

  private class Landmark extends Coordinate
  {
    final String title;

    public Landmark(String title, double longtitude, double latitude) {
      super(longtitude, latitude);
      this.title = title;
    }
  }
}
