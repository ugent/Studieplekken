import {CustomDate} from './helpers/CustomDate';

export interface Location {
  name: string;
  address: string;
  numberOfSeats: number;
  numberOfLockers: number;
  mapsFrame: string;
  descriptions: {};
  imageUrl: string;
  startPeriodLockers: CustomDate;
  endPeriodLockers: CustomDate;
}
