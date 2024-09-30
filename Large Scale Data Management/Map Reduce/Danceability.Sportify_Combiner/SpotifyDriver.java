package aueb.fnitsos.spotify;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class SpotifyDriver {

    private static String OUTPUT_PATH = "/user/hdfs/output/";
    private static Log log = LogFactory.getLog(SpotifyDriver.class);

    public static  void main(String[] args) throws Exception {

        log.info("######################### STARTING v3 #############################");
        System.setProperty("hadoop.home.dir", "/");

        // instantiate a configuration
        Configuration configuration = new Configuration();
        FileSystem hdfs = FileSystem.get(configuration);
        Path outputPath = new Path(OUTPUT_PATH);

        // If the output directory exists, delete it
        if (hdfs.exists(outputPath)) {
            hdfs.delete(outputPath, true); // true will recursively delete the directory
        }

        // instantiate a job
        Job job = Job.getInstance(configuration, "Spotify danceablity");

        // set job parameters
        job.setJarByClass(SpotifyMapReduceImpl.class);
        job.setMapperClass(SpotifyMapReduceImpl.SpotifyMapper.class);
        job.setCombinerClass(SpotifyMapReduceImpl.SpotifyCombiner.class);
        job.setReducerClass(SpotifyMapReduceImpl.SpotifyReducer.class);
        job.setOutputKeyClass(Text.class);
        //job.setOutputValueClass(SpotifyCombinerOutputValue.class);
        job.setMapOutputValueClass(SpotifyMapperOutputValue.class);
//        job.setMapOutputKeyClass(Text.class);

        // set io paths
        FileInputFormat.addInputPath(job, new Path("/user/fnitsos/songs/songs.csv"));
        FileOutputFormat.setOutputPath(job, outputPath);

        System.exit(job.waitForCompletion(true)? 0 : 1);
    }
}
