package wsn;

import net.tinyos.prowler.floodrouting.DataPacket;

public class Packet extends DataPacket {
    int size;
    int id;

    static int idCounter = 0;

    private Packet(int id, int size) {
        this.id = id;
        this.size = size;
    }

    public static Packet newInstance(int size) {
        return new Packet(idCounter++, size);
    }

    @Override
    public boolean equals(DataPacket packet) {
        return this.id == ((Packet)packet).id;
    }

    @Override
    public void copyTo(DataPacket packet) {
        packet = new Packet(id, size);
    }

    @Override
    public String toString() {
        return "Packet{id=" + id + ", size=" + size + "}";
    }
}
