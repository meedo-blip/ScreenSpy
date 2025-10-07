package jade;

import org.joml.Vector2f;

public class Transform {

    public Vector2f position, scale, centerR;
    public float z, rotate;


    public Transform() {
        init(new Vector2f(), 0f, new Vector2f(), new Vector2f(), 0f);
    }

    public Transform(Vector2f position, float z, Vector2f scale) {
        init(position, z, scale, new Vector2f(), 0f);
    }

    public Transform(Vector2f position, float z, Vector2f scale, Vector2f centerR) {
        init(position, z, scale, centerR, 0f);
    }

    public Transform(Vector2f position, float z, Vector2f scale, Vector2f centerR, float rotate) {
        init(position, z, scale, centerR, rotate);
    }

    public Transform(Transform transform) {
        init(new Vector2f(transform.position), transform.z, new Vector2f(transform.scale), new Vector2f(transform.centerR), transform.rotate);
    }

    public void init(Vector2f position, float z, Vector2f scale, Vector2f centerR, float rotate)  {
        this.position = position;
        this.scale = scale;
        this.z = z;
        this.centerR = centerR;
        this.rotate = rotate;
    }
}
