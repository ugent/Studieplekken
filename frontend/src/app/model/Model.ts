export abstract class Model {
    abstract fromJSON<T>(json: Record<string, any>): T;
}