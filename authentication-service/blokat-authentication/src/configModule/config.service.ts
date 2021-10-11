import { Injectable } from '@nestjs/common';
import { configuration as development_config } from '../../config/environment/environment.dev';
import { assert } from 'console';
import { Configuration } from './config';

const DEVELOPMENT_STRING = "development"
const TEST_STRING = 'test';


@Injectable()
export class ConfigService {

  constructor() {
  }

  public getCurrentConfiguration(): Configuration {
    return getConfig();
  }
}

const configMap: Map<string, Configuration> = new Map();
configMap.set(DEVELOPMENT_STRING, development_config);
configMap.set(TEST_STRING, development_config);



export function getConfig() {
    const environment = process.env.NODE_ENV || DEVELOPMENT_STRING;
    const config = configMap.get(environment);
    assert(config, 'This environment value is invalid');
    return config;
}