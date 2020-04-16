declare module "@capacitor/core" {
    interface PluginRegistry {
        AwesomePlugin: AwesomePluginPlugin;
    }
}
export interface AwesomePluginPlugin {
    echo(options: {
        value: string;
    }): Promise<{
        value: string;
    }>;
}
