import java.io.*;
import java.util.Scanner;

import org.apache.log4j.BasicConfigurator;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.AbstractDataModel;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;


//1000209 ratings, 3900 movies, 6040 users

public abstract class MoviesRecommender extends AbstractDataModel{
     
      public static void main(String args[]) throws IOException
      {

  int ageGroup=56;	//if the user inputs an invalid age group, he will be put in the group >56
  String name;
  boolean isFemale=false;
  
  Scanner scanner= new Scanner(System.in);
  System.out.println("Hello, what's your name?");
  name=scanner.nextLine();
  
  System.out.printf("Hello %s, first let me ask you some questions. What's your age?\n", name);
  int age=scanner.nextInt();
  if(age<18)
	  ageGroup=1;
  else if(age<25)
	  ageGroup=18;
  else if(age<35)
	  ageGroup=25;
  else if(age<45)
	  ageGroup=35;
  else if(age<50)
	  ageGroup=45;
  else if(age<56)
	  ageGroup=50;
  
  System.out.printf("Are you a female?(y/n)");
  String gender=scanner.next();
  if(gender.equals("y"))
	  isFemale=true;
 
  System.out.printf("So, you're %s, you are in the age group %d and you are ",name, ageGroup);
  if(isFemale)
	  System.out.println("female.\n");
  else
	  System.out.println("male.\n");
  

  
  /********let's find a user in the same age and with the same gender*********/
  
  String similarUserID="1";	//if no similar user can be found, use the first user
  File userFile = new File("/Users/valery/Documents/MoviesRS/Resources/users.txt");
  int userEnd=0;
  int totUsers=0;
  int totRec=0;
  
      BufferedReader userBr = new BufferedReader(new FileReader(userFile));
      String userLine = userBr.readLine();
      while (userLine != null) {
    	  totUsers++;
    	  if(isFemale){
    		  if(userLine.contains(",F,"+ageGroup))
    		  {
    			  userEnd=userLine.indexOf(',');
    			  similarUserID=userLine.substring(0,userEnd);
    		  	}
              userLine = userBr.readLine();
    		  }

           else
           {
        	   if(userLine.contains(",M,"+ageGroup))
             	{
             		userEnd=userLine.indexOf(',');
             		similarUserID=userLine.substring(0,userEnd);
             	}
        	   userLine = userBr.readLine();
           }
      }
      userBr.close();
      
      int myUserID=totUsers+1;
      

 

      /********let's find 10 movies which the similar user rated with 5**********/
      
      File ratingsFile = new File("/Users/valery/Documents/MoviesRS/Resources/ratings.txt");
      
      int ratingsSecondComma=0;
      int ratingsFirstComma=0;
      String moviesID[]=new String[10];
      int k=0;

          BufferedReader ratingsBr = new BufferedReader(new FileReader(ratingsFile));
          String ratingsLine = ratingsBr.readLine();
          
          while (ratingsLine != null && k<10) {
        	  if(ratingsLine.startsWith(similarUserID+",")){	//looking for my similar user's ratings
        		  ratingsFirstComma=ratingsLine.indexOf(","); //find the first comma, after the userID
        		  ratingsSecondComma=ratingsLine.indexOf(",", ratingsFirstComma+1); //find the second comma, before the rating
        		  
        		  if(ratingsLine.charAt(ratingsSecondComma+1)=='5') //if the user rated the movie with 5
        		  {
        			  moviesID[k]=ratingsLine.substring(ratingsFirstComma+1, ratingsSecondComma);	//save the movie ID
        			  k++;
        		  }
        	  }
        	  ratingsLine = ratingsBr.readLine();
          }
          ratingsBr.close();

          
          /*******let's ask the user his ratings for the 10 movies his similar user liked******/

          File moviesFile = new File("/Users/valery/Documents/MoviesRS/Resources/movies.txt");
          BufferedReader moviesBr = new BufferedReader(new FileReader(moviesFile));
          String moviesLine = moviesBr.readLine();
          String movieName;
          int moviesFirstComma;
          int moviesSecondComma;
          int myRating;
          PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter("/Users/valery/Documents/MoviesRS/Resources/ratings.txt",true)));
          
          while (moviesLine != null)
          {
        	  for(k=0;k<10;k++)
        	  {
        		  //if the movieID is found, find the corresponding title and ask the user
        		  if(moviesLine.startsWith(moviesID[k]+","))
        		  {
        			  moviesFirstComma=moviesLine.indexOf(","); //finding the commas,as before
            		  moviesSecondComma=moviesLine.indexOf(",", moviesFirstComma+1); //as before
        			  movieName=moviesLine.substring(moviesFirstComma+1, moviesSecondComma);
        			  System.out.println("Give me your rating for the movie \""+movieName+"\", please: (1-5 from bad to good, 0 if unknown)");
        			  myRating=scanner.nextInt();
        			  
        			  //save the rating only if the value is valid
        			  if(myRating<6 && myRating>0)
        			  {
        				  pw.println(myUserID+","+moviesID[k]+","+myRating);
        				  totRec++;
        			  }
        			  else if(myRating>5){
        				  System.out.println("Ooops!!! Please insert a rating between 1 and 5" + "\n");
        				  k--;
        			  }
        		  }
        	  }
              moviesLine = moviesBr.readLine();
          }
         // pw.close();
          moviesBr.close();

  
    //************if the user wants to look for movies to rate************//      
        
          BufferedReader myMovieBr = new BufferedReader(new FileReader(moviesFile));
          String myMovieLine = myMovieBr.readLine();
          boolean satisfied=false;
        
          boolean found=false;
          
          while (!satisfied)
          {
        	  System.out.println("You rated " + totRec + " movies. Do you want to rate other movies? (y/n)");
              String continueRating=scanner.next();
              found=false;
              if(!continueRating.equalsIgnoreCase("y"))
              {
            	  satisfied=true;
            	  break;
              }
        		  System.out.println("Tell me a movie you want to rate: (only 1 word please)");
        		  String myMovie=scanner.next();
        		  while ((myMovieLine != null) && !found)
                  {   
        		  //if a title containing the string is found, let the user rate this
        		  if(myMovieLine.contains(myMovie))
        		  {
        			  found=true;
        			  moviesFirstComma=myMovieLine.indexOf(","); //finding the commas,as before
            		  moviesSecondComma=myMovieLine.indexOf(",", moviesFirstComma+1); //as before
        			  movieName=myMovieLine.substring(moviesFirstComma+1, moviesSecondComma);
        			  System.out.println("Give me your rating for the movie \""+movieName+"\", please: (1-5)");
        			  myRating=scanner.nextInt();
        			  String movieID=myMovieLine.substring(0, moviesFirstComma);
        			  
        			  //save the rating only if the value is valid
        			  if(myRating<6 && myRating>0)
        			  {
        				  pw.println(myUserID+","+movieID+","+myRating);
        				  totRec++;
        			  }
        		  }
        		  myMovieLine = myMovieBr.readLine();
                  }
           }
          pw.close();
          myMovieBr.close();    
          
          
          System.out.printf("How many movies you would probably like do you want me to find? ");
          int noOfRecommendations=scanner.nextInt();
          
          
          
  /*******let's start the recommendation******/
      
          BasicConfigurator.configure();

           // int noOfRecommendations=10;
            long recMovies[]=new long[noOfRecommendations];
           
            try
            {
                  // Data model with the user ratings
                  FileDataModel dataModel = new FileDataModel(new File("/Users/valery/Documents/MoviesRS/Resources/ratings.txt"));
                  //We use Pearson Similarity algorithm
                  ItemSimilarity itemSimilarity = new PearsonCorrelationSimilarity(dataModel);
                  
                  // Create a recommender based on item similarity
                  Recommender recommender = new GenericItemBasedRecommender(dataModel, itemSimilarity);
                                  
                 for(int l=0;l<noOfRecommendations;l++)
                 {
                	 //save the IDs of the recommended movies into the array recMovies[]
                	 recMovies[l]=recommender.recommend(myUserID, noOfRecommendations).get(l).getItemID();
                 }
                 
                 System.out.printf("\n\nDear %s, I recommend you the following movies: \n\n", name);

                 //reading again the movies file to find the titles of the recommended movies
                 BufferedReader titleBr = new BufferedReader(new FileReader(moviesFile));
                 String TitleLine = titleBr.readLine();
                 
                 while (TitleLine != null)
                 {
               	  for(int l=0;l<noOfRecommendations;l++)
               	  {
               		  if(TitleLine.startsWith(recMovies[l]+","))
               		  	{
               			  moviesFirstComma=TitleLine.indexOf(","); 
               			  moviesSecondComma=TitleLine.indexOf(",", moviesFirstComma+1);
               			  movieName=TitleLine.substring(moviesFirstComma+1, moviesSecondComma);
               			  System.out.println(movieName); 
               		  	}
               	  }
               	  TitleLine=titleBr.readLine();
                 }
                 
                 titleBr.close();
                   
            } catch (TasteException e) {
                  e.printStackTrace();
            }
      }
                 
}
