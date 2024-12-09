export enum OrderDirection {
    DESC = 0,
    ASC = 1
}

export function booleanSorter<T>(mapper: (T: T) => boolean): (a: T, b: T) => (number) {
    return (a: T, b: T) => {
        if (mapper(b) === mapper(a)) {
            return 0;
        }
        if (mapper(a)) {
            return -1;
        }
        if (mapper(b)) {
            return 1;
        }
    };
}

export function genericSorter(a: any, b: any, direction: OrderDirection): number {
    const ascending = OrderDirection.ASC === direction;

    if (typeof a === 'number') {
        return ascending ? a - b : b - a;
    }

    if (typeof a === 'string') {
        return ascending ? a.localeCompare(b) : b.localeCompare(a);
    }

    if (a instanceof Date) {
        return ascending ? a.getTime() - b.getTime() : b.getTime() - a.getTime();
    }

    // If the types are not directly comparable, convert them to strings and compare
    return ascending ? String(a).localeCompare(String(b)) : String(b).localeCompare(String(a));
}

export function escapeRegex(str: string): string {
    return str.replace(/[/\-\\^$*+?.()|[\]{}]/g, '\\$&');
}
