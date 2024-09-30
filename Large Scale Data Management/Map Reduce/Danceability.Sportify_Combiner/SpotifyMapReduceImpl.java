package aueb.fnitsos.spotify;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class SpotifyMapReduceImpl {

    private static Log log = LogFactory.getLog(SpotifyMapReduceImpl.class);

    /**
     *  The mapper taxes as an input a key value pair, where the key is of type LongWritable
     *  and represent the line number of the csv file and the value is a text that contains the comma separated values.
     *
     *   The mapper produces as an output a key value pair, where the key is a text that combines the country code, year & month
     *   and the value is the composite DancebleWritable class that contains the song and it's danceability
     */
    public static class SpotifyMapper extends Mapper<LongWritable, Text, Text, SpotifyMapperOutputValue> {
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            // Skip the first line of the csv because of headers.
            if (key.get()==0){
                System.out.println(value);
                return;
            }

            // parse the csv line
            String[] words = value.toString().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

            // get country code and remove quotes, if the country is null skip the record
            String country = removeQuotes(words[6]);
            if (country == null) {
                return;
            }

            // get the month and year for
            String month = removeQuotes(words[7].substring(0,8));
            // create the text key that will be aggregated into the reducer
            String myKey = country.concat("_").concat(month);
            //Get the danceability
            String danceability = removeQuotes(words[13]);
            // Get the song
            String song = removeQuotes(words[1]);
            // Write records to context for the reducer to read them
            context.write(new Text(myKey), new SpotifyMapperOutputValue(song, Float.parseFloat(danceability)));

        }
    }


    public static class SpotifyCombiner extends Reducer<Text, SpotifyMapperOutputValue, Text, SpotifyMapperOutputValue> {

        @Override
        public void reduce(Text key, Iterable<SpotifyMapperOutputValue> values, Context context) throws IOException, InterruptedException {
            float maxD = 0.0f;
            String song = "";
            float danceabilitySum = 0.0f;
            int numberOfSongs = 0;


            // Loop through the records of the same key (same country, year and month), find the max and the avg danceability
            for (SpotifyMapperOutputValue value : values) {
                log.info("Reducer : key "+ key + " | value " + value.toString());
                float danceability = value.getBatchMaxDanceability();
                danceabilitySum = danceabilitySum + danceability;
                numberOfSongs++;
                if (danceability > maxD) {
                    maxD = danceability;
                    song = value.getBatchMostDancaebleSong();
                }
            }
            context.write(key,  new SpotifyMapperOutputValue(song, maxD, danceabilitySum, numberOfSongs));
        }
    }

    /**
     * The reducer takes as a key a concatenated text(String) the year month & country code and as value the composite type of DancebleWritable class.
     * The output is the key (same as the mapper) and the value o concatenated string of the song, the max and the average danceabiity.
     */
    public static class SpotifyReducer extends Reducer<Text, SpotifyMapperOutputValue, Text, Text> {
        @Override
        public void reduce(Text key, Iterable<SpotifyMapperOutputValue> values, Context context) throws IOException, InterruptedException {
            // sum up counts for the key
            float maxD = 0.0f;
            String song = "";
            float danceabilitySum = 0.0f;
            int numberOfSongs = 0;


            // Loop through the records of the same key (same country, year and month), find the max and the avg danceability
            for (SpotifyMapperOutputValue value : values) {
                log.info("Reducer : key "+ key + " | value " + value.toString());
                float danceability = value.getBatchMaxDanceability();
                danceabilitySum = danceabilitySum + value.getBatchDanceabilitySum();
                numberOfSongs = numberOfSongs + value.getCount();
                if (danceability > maxD) {
                    maxD = danceability;
                    song = value.getBatchMostDancaebleSong();
                }
            }
            // Create the reducer's output text
            String finalText = "song= "+ song + " , max danceablity= "+ maxD + ", average danceability= "+ danceabilitySum/numberOfSongs;
            // Write the output records in the context (country_year_month, song_maxDanceability_avgDanceability)
            context.write(key,  new Text(finalText));
        }
    }

    private static String removeQuotes(String str){
        return str.replaceAll("\"", "");
    }
}
