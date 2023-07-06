export function booleanSorter<T>(mapper: (T: T) => boolean) {
    return (a: T, b:T) => {
        if(mapper(a) == mapper(b))
            return 0;
        if(mapper(a))
            return -1
        if(mapper(b))
            return 1;
    }
}