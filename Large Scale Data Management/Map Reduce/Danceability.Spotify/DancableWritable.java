package aueb.fnitsos.spotify;

import org.apache.hadoop.io.Writable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 This composite writable type is created in order to pass the two required arguments(song & danceability)
 from the map to the reduce function.
 **/
public class DancableWritable implements Writable {
    private String song;
    private String danceability;

    public DancableWritable() {
    }

    public DancableWritable(String field1, String field2) {
        this.song = field1;
        this.danceability = field2;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(song);
        out.writeUTF(danceability);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        song = in.readUTF();
        danceability = in.readUTF();
    }

    public String getSong() {
        return song;
    }

    public String getDanceability() {
        return danceability;
    }

    public float getDanceabilityAsFloat() {
        return Float.parseFloat(danceability);
    }

    @Override
    public String toString() {
        return
                "song='" + song + '\'' +
                ", danceability='" + danceability;
    }
}

