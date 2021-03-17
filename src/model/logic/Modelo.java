package model.logic;

import java.io.FileReader;
import java.io.Reader;
import java.util.Comparator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import model.data_structures.ArregloDinamico;
import model.data_structures.ILista;
import model.data_structures.ListaEncadenada;
import model.utils.Ordenamiento;

/**
 * Definicion del modelo del mundo
 *
 */
public class Modelo {
	/**
	 * Atributos del modelo del mundo
	 */
	private ILista<YoutubeVideo> orderedByLike;
	private ILista<YoutubeVideo> orderedByViews;
	private ILista<Country> countries;
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
		orderedByLike = new ArregloDinamico<YoutubeVideo>(100, true);
		orderedByViews = new ArregloDinamico<YoutubeVideo>(100, true);
		categories = new ListaEncadenada<Category>();
		countries = new ListaEncadenada<Country>();
		// datos = new ListaEncadenada<YoutubeVideo>();
		cargar();
	}

	/**
	 * Servicio de consulta de numero de elementos presentes en el modelo
	 * 
	 * @return numero de elementos presentes en el modelo
	 */
	public int darTamano() {
		return orderedByLike.size();
	}

	/**
	 * Requerimiento de agregar dato
	 * 
	 * @param dato
	 */
	public void agregar(YoutubeVideo dato) {
		orderedByLike.addLast(dato);
	}

	/**
	 * Requerimiento buscar dato
	 * 
	 * @param dato Dato a buscar
	 * @return dato encontrado
	 */
	public int buscar(YoutubeVideo dato) {
		return orderedByLike.isPresent(dato);
	}

	/**
	 * Requerimiento eliminar dato
	 * 
	 * @param dato Dato a eliminar
	 * @return dato eliminado
	 */
	public YoutubeVideo eliminar(YoutubeVideo dato) {
		int pos = orderedByLike.isPresent(dato);
		return orderedByLike.deleteElementPos(pos);
	}

	public void cargar() {
		System.out.println("Start upload");
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
			in = new FileReader("./data/videos-small.csv");
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
				Country newCountry = new Country(country, videoTags);
				int countryPos = countries.isPresent(newCountry);
				Country countryObj = countries.getElement(countryPos);
				if (countryObj == null) {
					countries.addLast(newCountry);
					countryObj = newCountry;
				}
				Category newCategory = new Category(category_id, "");
				int categoryPos = categories.isPresent(newCategory);
				Category categoryObj = categories.getElement(categoryPos);
				if (categoryObj == null) {
					throw new Error("El Id de categoría " + category_id + " no es un id valido");
				}
				countryObj.addToLists(video);
				categoryObj.addToLists(video);
				orderedByLike.addLast(video);
				orderedByViews.addLast(video);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		this.orderLists();
		System.out.println("Creación: " + (end - start));
		System.out.println("size: " + orderedByLike.size() + " Dato1: " + orderedByLike.getElement(0));
	}

	public void orderLists() {
		long start = System.currentTimeMillis();
		System.out.println("Order");
		Comparator<YoutubeVideo> comparatorLikes = new YoutubeVideo.ComparadorXLikes();
		Comparator<YoutubeVideo> comparatorViews = new YoutubeVideo.ComparadorXViews();
		sorter.quickSort(orderedByLike, comparatorLikes, false);
		System.out.println("Likes ordered");
		sorter.quickSort(orderedByViews, comparatorViews, false);
		System.out.println("Views ordered");
		for (int i = 0; i < countries.size(); i++) {
			countries.getElement(i).orderLists();
		}
		System.out.println("Countries ordered");
		for (int i = 0; i < categories.size(); i++) {
			categories.getElement(i).orderLists();
		}
		System.out.println("Countries ordered");
		long end = System.currentTimeMillis();
		System.out.println("Ordenamiento1: " + (end - start));
	}

	public String req1(String category_name, String country, int n) {
		int categoryId = -1;
		for (int i = 0; i < categories.size(); i++) {
			Category temp = categories.getElement(i);
			if (temp.getName().trim().compareToIgnoreCase(category_name) == 0) {
				categoryId = temp.getId();
				break;
			}
		}
		Country newCountry = new Country(country, "");
		int countryPos = countries.isPresent(newCountry);
		Country countryObj = countries.getElement(countryPos);
		if (countryObj == null) {
			throw new Error("No se econtró país con nombre " + country);
		}
		ILista<YoutubeVideo> resList = countryObj.getCategoryViews(categoryId).sublista(n);
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

	public String req2(String country) {
		Country newCountry = new Country(country, "");
		int countryPos = countries.isPresent(newCountry);
		Country countryObj = countries.getElement(countryPos);
		if (countryObj == null) {
			throw new Error("No se econtró país con nombre " + country);
		}
		ILista<YoutubeVideo> resList = countryObj.getVideosbyTrending().sublista(1);
		String res = "title" + "\t - \t" + "channel_title" + "\t - \t" + "category_id" + "\t - \t" + "Días" + "\n";
		for (int i = 0; i < resList.size(); i++) {
			YoutubeVideo yt = resList.getElement(i);
			res += yt.getTitle() + "\t" + yt.getChannel_title() + "\t" + yt.getCategory_id() + "\t"
					+ yt.getTrendingDays() + "\n";
		}
		return res;
	}

	public String req3(String category_name) {
		Category category = null;
		for (int i = 0; i < categories.size(); i++) {
			Category temp = categories.getElement(i);
			if (temp.getName().trim().compareToIgnoreCase(category_name) == 0) {
				category = temp;
				break;
			}
		}
		if (category == null) {
			throw new Error("Categoría " + category_name + " No es valida");
		}
		ILista<YoutubeVideo> resList = category.getVideosTrending().sublista(1);
		String res = "title" + "\t - \t" + "channel_title" + "\t - \t" + "category_id" + "\t - \t" + "Días" + "\n";
		for (int i = 0; i < resList.size(); i++) {
			YoutubeVideo yt = resList.getElement(i);
			res += yt.getTitle() + "\t\t" + yt.getChannel_title() + "\t" + yt.getCategory_id() + "\t"
					+ yt.getTrendingDays() + "\n";
		}
		return res;
	}

	public String req4(String country, String tag, int n) {
		Country newCountry = new Country(country, "");
		int countryPos = countries.isPresent(newCountry);
		Country countryObj = countries.getElement(countryPos);
		if (countryObj == null) {
			throw new Error("No se econtró país con nombre " + country);
		}
		ILista<YoutubeVideo> resList = countryObj.getTagViews(tag).sublista(n);
		String res = "title" + "\t - \t" + "channel_title" + "\t - \t" + "publish_time" + "\t - \t" + "views"
				+ "\t - \t" + "likes" + "\t - \t" + "dislikes" + "\n";
		for (int i = 0; i < resList.size(); i++) {
			YoutubeVideo yt = resList.getElement(i);
			res += yt.getTitle() + "\t" + yt.getChannel_title() + "\t" + yt.getPublish_time() + "\t" + yt.getViews()
					+ "\t" + yt.getLikes() + "\t" + yt.getDislikes() + "\t" + yt.getTagsString() + "\n";
		}
		return res;
	}

	@Override
	public String toString() {
		return countries.toString();
	}
}
