package aueb.fnitsos.spotify;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class SpotifyMapperOutputValue implements Writable {

    public SpotifyMapperOutputValue() {
    }

    public SpotifyMapperOutputValue(String batchMostDancaebleSong, float batchMaxDanceability) {
        this.batchMostDancaebleSong = batchMostDancaebleSong;
        this.batchMaxDanceability = batchMaxDanceability;
    }

    public SpotifyMapperOutputValue(String song, float danceability, float sum, int count) {
        this.batchMostDancaebleSong = song;
        this.batchMaxDanceability = danceability;
        this.batchDanceabilitySum = sum;
        this.count = count;
    }

    private String batchMostDancaebleSong;
    private float batchMaxDanceability;
    private float batchDanceabilitySum;
    private int count;

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(batchMostDancaebleSong);
        out.writeFloat(batchMaxDanceability);
        out.writeFloat(batchDanceabilitySum);
        out.writeInt(count);

    }

    @Override
    public void readFields(DataInput in) throws IOException {
        batchMostDancaebleSong = in.readUTF();
        batchMaxDanceability = in.readFloat();
        batchDanceabilitySum = in.readFloat();
        count = in.readInt();
    }

    public String getBatchMostDancaebleSong() {
        return batchMostDancaebleSong;
    }

    public float getBatchMaxDanceability() {
        return batchMaxDanceability;
    }

    public float getBatchDanceabilitySum() {
        return batchDanceabilitySum;
    }

    public int getCount() {
        return count;
    }
}
