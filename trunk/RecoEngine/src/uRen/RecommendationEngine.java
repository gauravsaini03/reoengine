package uRen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;




public class RecommendationEngine {
	
	DatabaseHandler db;

	public RecommendationEngine() {
		Logger.Log("recommendation engine created");
		db = new DatabaseHandler();
		if (db == null) {
			Logger.Log("db handler NOT created!");
		}
	}

	public ArrayList<Album> GetRecommendations_alg1(Customer cust, String artist) {
		//ArrayList<Album> results = new ArrayList<Album>();
		//results.add(new Album(99, "darkside of the moon", "pink floyd", "rock", 100));
		//Purchase list = db.GetAlbumsPurchasedByCustomer(cust);
		ArrayList<Purchase> recoPool = db.GetPurchasesByArtist(cust.getIdCustomer(), artist);
		//get a list of all customers that have purchased an album by "artist" excluding the current customer
		//in that list of customers, find the largest common subset... [find everything common to all of them!?]
		
		return DoT3hRecommendationMagic(recoPool);
		//return list.getPurchases();
	}
	
	public ArrayList<Album> GetRecommendations_alg2a(Customer cust, String genre) {
		ArrayList<Album> results = new ArrayList<Album>();
		//results.add(new Album(98, "darkside of the moon2a", "pink floyd", "rock", 100));
		return results;
	}
	
	public ArrayList<Album> GetRecommendations_alg2b(Customer cust) {
		ArrayList<Album> results = new ArrayList<Album>();
		//results.add(new Album(97, "darkside of the moon2b", "pink floyd", "rock", 100));
		return results;
	}
	
	public ArrayList<Album> GetRecommendations_alg3(Customer cust) {
		ArrayList<Album> results = new ArrayList<Album>();
		//results.add(new Album(96, "darkside of the moon3", "pink floyd", "rock", 100));
		return results;
	}
	
	public ArrayList<Album> GetRecommendations_alg4() {
		ArrayList<Album> results = new ArrayList<Album>();
		//results.add(new Album(95, "darkside of the moon4", "pink floyd", "rock", 100));
		return results;
	}
	
	//now for fun stuff! :)
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ArrayList<Album> DoT3hRecommendationMagic(ArrayList<Purchase> data) {
		ArrayList<Album> recommendationPool = new ArrayList<Album>();
		
		ArrayList<ArrayList<Album>> sourcePool = new ArrayList<ArrayList<Album>>();
		for (Purchase purchase : data) {
			if (purchase != null) {
				sourcePool.add(purchase.getPurchases());
			}
		}
		
		//im gonna try to do this in O(n*m) first, and then see where i can improve... later
		//for class projects -> [first]make it work. [second]make it elegant. [third]make it fast.
		
		//i have 2 strategies in mind...
		//first is more efficient but may not be as accurate in its recommendations
		//second is inefficient but its recommendations will be more accurate
		//im implementing both, and commenting out the inefficient one.
		//maybe i can do some runtime switching based on some logic ? i dono... first [quick] one for now
		
		HashMap<Integer, Integer> ranks = new HashMap<Integer, Integer>(); //STUPID java does not support a hashmap with primitives!!!

		/*
		 * 
		 * STRATEGY 1: rank all the albums according to popularity...
		 * 
		 */
		for (int outer = 0; outer < sourcePool.size(); outer++) {
			ArrayList<Album> listToCompare = sourcePool.get(outer);
			for (Album album : listToCompare) {
				//found.. increase the rank for album.getID() by 1
				Integer AlbumID = new Integer(album.getID()); //STUPID java
				if (ranks.containsKey(AlbumID)) {
					Integer frequency = ranks.get(AlbumID);
					frequency++;
					ranks.put(AlbumID, frequency);
				}
				else {
					ranks.put(AlbumID, 0);
				}
			} //end for each album
		} //end for each list
		
		/*
		 * 
		 * STRATEGY 2: rank only the common albums according to popularity
		 * 
		 */
		/*
		for (int outer = 0; outer < sourcePool.size(); outer++) {
			ArrayList<Album> listToCompare = sourcePool.get(outer);
			for (Album album : listToCompare) {
				//check if this album is in any of the remaining lists...
				for (int inner = 0; inner < sourcePool.size(); inner++) {
					if (inner != outer) { //skip the current list (it will obviously exist!)
						ArrayList<Album> targetList = sourcePool.get(inner);
						for (Album targetAlbum : targetList) {
							if (album.getID() == targetAlbum.getID()) {
								//found.. increase the rank for album.getID() by 1
								Integer AlbumID = new Integer(album.getID()); //STUPID java
								if (ranks.containsKey(AlbumID)) {
									Integer frequency = ranks.get(AlbumID);
									frequency++;
									ranks.put(AlbumID, frequency);
								}
								else {
									ranks.put(AlbumID, 0);
								}
								break; //stop searching
							} // end if found
						} // end for each album in the inner list
					} //end if
				} // end for inner
			} // end for each album in outer list
		} // end for outer
		*/
		
		//at this point 'ranks' has all the information i need... i just need to pick the highest [or lowest ;)] X ranks,
		//and that, ladies and gentlemen, is THE recommendation pool! :D
		System.out.println("hh");
		//sort the hashmap based on values
		ArrayList sortedArrayList = new ArrayList( ranks.entrySet() );  

		Collections.sort( sortedArrayList , new Comparator() {  
			public int compare( Object o1 , Object o2 )  
			{  
				Map.Entry e1 = (Map.Entry)o1 ;  
				Map.Entry e2 = (Map.Entry)o2 ;  
				Integer first = (Integer)e1.getValue();  
				Integer second = (Integer)e2.getValue();  
				return second.compareTo( first );  
			}  
		});

		LinkedHashMap<Integer, Integer> sortedRanks = new LinkedHashMap<Integer, Integer>();
		Iterator i = sortedArrayList.iterator();  
		while ( i.hasNext() )  
		{  
			//System.out.println( (Map.Entry)i.next() );
			Map.Entry entry = (Map.Entry)i.next();
			sortedRanks.put((Integer)entry.getKey(), (Integer)entry.getValue());
		}
		
		//now just search for the best ranked chaps and return them :)
		int forgiveness = SessionSettings.SonnyCorleone; 
		int oldFreq = -1;
		for(Integer key : sortedRanks.keySet()) {
			
			Album album = db.GetAlbumByID(key.intValue());
			int freq = sortedRanks.get(key).intValue();
			
			if (freq != oldFreq) {
				oldFreq = freq;
				forgiveness--;
				if (forgiveness < 0) break; //im done with recommendations
			}
			
			Logger.Log( album.getArtistName() + ", " +
						album.getAlbumName() + " - "+ freq);
			
			recommendationPool.add(album);
		}//end for
		
		return recommendationPool;
	}


	public Purchase GetPurchasesByCustomer(Customer cust) {
		return db.GetAlbumsPurchasedByCustomer(cust);
	}
	
	public void TestDBInsertUser(String fname, String lname, String nname, String email) {
		db.InsertIntoCustomerTable(fname, lname, nname, email);
	}
	
	public Customer GetCustomerByEmailAddress(String email) {
		return db.GetCustomerByEmailAddress(email);
	}
	
	public Customer GetCustomerByID(int id) {
		return db.GetCustomerByID(id);
	}
	
}
