import {Locale} from './helpers/Locale';

export type Translatable =  {
    translations: {
        [key in Locale]: string;
    }
}
