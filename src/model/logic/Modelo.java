package model.logic;

import java.io.FileReader;
import java.io.Reader;
import java.util.Comparator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import model.data_structures.ArregloDinamico;
import model.data_structures.ILista;
import model.data_structures.ITablaSimbolos;
import model.data_structures.ListaEncadenada;
import model.data_structures.TablaHashLinearProbing;
import model.data_structures.TablaHashSeparateChaining;
import model.utils.Ordenamiento;

/**
 * Definicion del modelo del mundo
 *
 */
public class Modelo {
	/**
	 * Atributos del modelo del mundo
	 */
	private ITablaSimbolos<String, ILista<YoutubeVideo>> videosChaining;
	private ITablaSimbolos<String, ILista<YoutubeVideo>> videosProbing;
	private ILista<Category> categories;
	/**
	 * Atributos del modelo del mundo
	 */
	private Ordenamiento<YoutubeVideo> sorter;

	/**
	 * Constructor del modelo del mundo con capacidad predefinida
	 */
	public Modelo() {
		sorter = new Ordenamiento<YoutubeVideo>();
		videosChaining = new TablaHashSeparateChaining<>(50000);
		videosProbing = new TablaHashLinearProbing<>(50000);
		categories = new ListaEncadenada<Category>();
		// datos = new ListaEncadenada<YoutubeVideo>();
		cargar();
	}

	/**
	 * Servicio de consulta de numero de elementos presentes en el modelo
	 * 
	 * @return numero de elementos presentes en el modelo
	 */
	public int darTamano() {
		return videosChaining.size();
	}

	public void cargar() {
		System.out.println("Start upload");
		int count = 0;
		Reader in;
		long start = System.currentTimeMillis();
		try {
			in = new FileReader("./data/category-id.csv");
			Iterable<CSVRecord> categoriesCsv = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
			for (CSVRecord record : categoriesCsv) {
				String id = record.get(0);
				String name = record.get(1);
				Category category = new Category(id, name);
				categories.addLast(category);
			}
			in = new FileReader("./data/videos-small-big.csv");
			Iterable<CSVRecord> videosCsv = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
			for (CSVRecord record : videosCsv) {
				String trending_date = record.get(1);
				String video_id = record.get("video_id");
				String title = record.get(2);
				String channel_title = record.get(3);
				String category_id = record.get(4);
				String publish_time = record.get(5);
				String videoTags = record.get(6);
				String views = record.get(7);
				String likes = record.get(8);
				String dislikes = record.get(9);
				String comment_count = record.get(10);
				String thumbnail_link = record.get(11);
				String comments_disabled = record.get(12);
				String ratings_disabled = record.get(13);
				String video_error_or_removed = record.get(14);
				String descriptio = record.get(15);
				String country = record.get(16);
				YoutubeVideo video = new YoutubeVideo(video_id, trending_date, title, channel_title, category_id,
						publish_time, videoTags, views, likes, dislikes, comment_count, thumbnail_link,
						comments_disabled, ratings_disabled, video_error_or_removed, descriptio, country);
				Category category = categories.find(new Category("" + video.getCategory_id(), ""));
				ILista<YoutubeVideo> videoList = videosChaining
						.get(country.trim().toUpperCase() + category.getName().trim().toUpperCase());
				if (videoList == null) {
					videoList = new ArregloDinamico<YoutubeVideo>(100);
					videosChaining.put(country.trim().toUpperCase() + category.getName().trim().toUpperCase(),
							videoList);
				}
				videoList.addLast(video);
				count++;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		System.out.println("Creación: " + (end - start));
		System.out.println("size: " + count + " Duplas: " + videosChaining.size());
		// System.out.println(videosChaining.toString());
	}

	public String req1(String category_name, String country, int n) {
		ILista<YoutubeVideo> list = videosChaining
				.get(country.trim().toUpperCase() + category_name.trim().toUpperCase());
		if (list == null) {
			return null;
		}
		ILista<YoutubeVideo> resList = list.sublista(n);
		String res = "trending_date" + "\t - \t" + "title" + "\t - \t" + "channel_title" + "\t - \t" + "publish_time"
				+ "\t - \t" + "views" + "\t - \t" + "likes" + "\t - \t" + "dislikes" + "\n";
		for (int i = 0; i < resList.size(); i++) {
			YoutubeVideo yt = resList.getElement(i);
			res += yt.getTrending_date().toString() + "\t" + yt.getTitle() + "\t" + yt.getChannel_title() + "\t"
					+ yt.getPublish_time() + "\t" + yt.getViews() + "\t" + yt.getLikes() + "\t" + yt.getDislikes()
					+ "\n";
		}
		return res;
	}

	@Override
	public String toString() {
		return videosChaining.toString();
	}
}
