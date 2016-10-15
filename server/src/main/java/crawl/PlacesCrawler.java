package crawl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.conditional.ITagNodeCondition;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class PlacesCrawler {
  public static void main(String[] args) throws IOException {
    PlacesCrawler crawler = new PlacesCrawler();
    JSONArray json = crawler.crawlCity("nl/amsterdam.html");
    try (Writer fileWriter = new FileWriter(new File("AmsterdamHotels.txt")))
    {
      fileWriter.write(json.toString(2));
    }
  }

  public JSONArray crawlCityCached(final String city) throws IOException {
    File cacheFile = new File("cache/" + String.valueOf(city.replaceAll("[?.\\\\/=&:]+", "_")) + ".txt");
    if (!cacheFile.exists())
    {
      System.out.println("Create file: " + cacheFile.getCanonicalPath());
      cacheFile.getParentFile().mkdirs();
      JSONArray data = crawlCity(city);
      Files.write(cacheFile.toPath(), data.toString(2).getBytes(StandardCharsets.UTF_8));
      return data;
    }
    else
      return new JSONArray(new JSONTokener(new FileInputStream(cacheFile)));
  }

  public JSONArray crawlCity(final String city) throws IOException {
    TagNode mainPage = crawlPage("http://www.booking.com/destinationfinder/cities/" + city);
    List landmarks = mainPage.getElementList(new TagWithClass("div", "card-landmark"), true);
    JSONArray json = new org.json.JSONArray();
    for (Object mark: landmarks)
    {
      JSONObject oneMark = new org.json.JSONObject();
      TagNode markNode = (TagNode) mark;
      Object imgLink = markNode.getElementList(new TagWithClass("img", "card-landmark__photo"), true).get(0);
      oneMark.put("image", ((TagNode)imgLink).getAttributeByName("src"));
      Object title = markNode.getElementList(new TagWithClass("a", "card-landmark__title"), true).get(0);
      oneMark.put("link", ((TagNode)title).getAttributeByName("href"));
      oneMark.put("title", ((TagNode)title).findElementByName("span", false).getText());
      Object type = markNode.getElementList(new TagWithClass("p", "card-landmark__subtitle"), true).get(0);
      oneMark.put("type", ((TagNode)type).getText().toString().trim());
      Object desc = markNode.getElementList(new TagWithClass("p", "card-landmark__text"), true).get(0);
      oneMark.put("description", ((TagNode)desc).getText().toString().trim());

      Object searchLink = markNode.getElementList(new TagWithClass("a", "card-landmark__cta"), true).get(0);
      String link = ((TagNode)searchLink).getAttributeByName("href");
      oneMark.put("hotels_link", link);
      crawlCoordinates("http://www.booking.com" + link, oneMark);

      json.put(oneMark);
    }
    return json;
  }

  private final HttpClient client = HttpClientBuilder.create().build();
  private final HtmlCleaner cleaner = new HtmlCleaner();

  private TagNode crawlPage(String page) throws IOException {
    try (InputStream content = client.execute(new HttpGet(page)).getEntity().getContent())
    {
      return cleaner.clean(content);
    }
  }

  Pattern placeExpr = Pattern.compile("place_id=\\d+;place_id_lat=([\\d\\.]+);place_id_lon=([\\d\\.]+)");

  private void crawlCoordinates(String page, org.json.JSONObject place) throws IOException {
    try (InputStream content = client.execute(new HttpGet(page)).getEntity().getContent())
    {
      BufferedReader rdr = new BufferedReader(new InputStreamReader(content));
      Optional<String> coordsLine = rdr.lines().filter(s -> s.contains("place_id_lat") && s.contains("place_id_lon")).findFirst();
      if (!coordsLine.isPresent())
        throw new IllegalStateException();
      final Matcher matcher = placeExpr.matcher(coordsLine.get());
      if (!matcher.find())
        throw new IllegalStateException();
      place.put("latitude", matcher.group(1));
      place.put("longtitude", matcher.group(2));
    }
  }

  private static class TagWithClass implements ITagNodeCondition {
    private final String tagName;
    private final String clazz;

    public TagWithClass(String tagName, String clazz) {
      this.tagName = tagName;
      this.clazz = clazz;
    }

    @Override
    public boolean satisfy(TagNode tagNode) {
      if (!tagName.equalsIgnoreCase(tagNode.getName()))
        return false;
      final String allClasses = tagNode.getAttributeByName("class");
      if (allClasses == null)
        return false;
      String[] classes = allClasses.split(" +");
      for (String cls: classes)
        if (clazz.equalsIgnoreCase(cls))
          return true;
      return false;
    }

  }
}
