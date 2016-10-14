package com.booking.hackathon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    try (PrintWriter out = response.getWriter()) {
      final String postData = getPostData(request);
      List<Coordinate> placesCoordinates;
      if (!postData.trim().isEmpty())
      {
        JSONObject obj = new org.json.JSONObject(postData);
        JSONArray places = obj.getJSONArray("places");
        placesCoordinates = parsePlaces(places);
      }
      else
        placesCoordinates = Arrays.asList(new Coordinate(4.8930, 52.36));
      JSONArray hotels = HotelsSorter.sort(placesCoordinates, BookingHotels.forCity(-2140479, 7));
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
      result.add(new Coordinate(place.getDouble("longtitude"), place.getDouble("latitude")));
    }
    return result;
  }

}
