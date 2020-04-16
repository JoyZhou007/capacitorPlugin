import { WebPlugin } from '@capacitor/core';
import { AwesomePluginPlugin } from './definitions';
export declare class AwesomePluginWeb extends WebPlugin implements AwesomePluginPlugin {
    constructor();
    echo(options: {
        value: string;
    }): Promise<{
        value: string;
    }>;
}
declare const AwesomePlugin: AwesomePluginWeb;
export { AwesomePlugin };
