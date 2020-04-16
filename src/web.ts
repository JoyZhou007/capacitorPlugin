import { WebPlugin } from '@capacitor/core';
import { AwesomePluginPlugin } from './definitions';

export class AwesomePluginWeb extends WebPlugin implements AwesomePluginPlugin {
  constructor() {
    super({
      name: 'AwesomePlugin',
      platforms: ['web']
    });
  }

  async echo(options: { value: string }): Promise<{value: string}> {
    console.log('ECHO', options);
    return options;
  }
}

const AwesomePlugin = new AwesomePluginWeb();

export { AwesomePlugin };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(AwesomePlugin);
