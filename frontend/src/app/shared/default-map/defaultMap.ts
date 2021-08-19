export class DefaultMap<K, V> extends Map<K, V[]> {
    addValueAsList(key: K, value: V) {
        const previousList = this.get(key)
        const previousOrDefaultList = previousList ? previousList:[];

        this.set(key, [...previousOrDefaultList, value]);
        return this.get(key);
    }
}