declare module 'three/examples/jsm/controls/OrbitControls.js' {
    import { Camera, MOUSE, TOUCH, Object3D } from 'three';
    export class OrbitControls {
        constructor(object: Camera, domElement?: HTMLElement);
        object: Camera;
        domElement: HTMLElement | HTMLDocument;
        enabled: boolean;
        target: Object3D['position'];
        update(): void;
        dispose(): void;
    }
}

declare module 'three/examples/jsm/loaders/GLTFLoader.js' {
    import { LoadingManager, Group } from 'three';
    export class GLTFLoader {
        constructor(manager?: LoadingManager);
        load(url: string, onLoad: (gltf: any) => void, onProgress?: (event: ProgressEvent) => void, onError?: (event: ErrorEvent) => void): void;
        register(callback: (parser: any) => any): GLTFLoader;
        parse(data: any, path: string, onLoad: (gltf: any) => void): void;
    }
}

declare module 'three/examples/jsm/loaders/MMDLoader.js' {
    import { LoadingManager, Mesh, FileLoader } from 'three';
    export class MMDLoader {
        constructor(manager?: LoadingManager);
        load(url: string, onLoad: (mesh: Mesh) => void, onProgress?: (event: ProgressEvent) => void, onError?: (event: ErrorEvent) => void): void;
        loadWithAnimation(modelUrl: string, vmdUrl: string | string[], onLoad: (object: any) => void, onProgress?: (event: ProgressEvent) => void, onError?: (event: ErrorEvent) => void): void;
    }
}

declare module 'three/examples/jsm/controls/PointerLockControls.js' {
    import { Camera } from 'three';
    export class PointerLockControls {
        constructor(camera: Camera, domElement?: HTMLElement);
        object: Camera;
        domElement: HTMLElement;
        isLocked: boolean;
        lock(): void;
        unlock(): void;
        moveForward(distance: number): void;
        moveRight(distance: number): void;
        addEventListener(type: string, listener: (event?: any) => void): void;
        removeEventListener(type: string, listener: (event?: any) => void): void;
        dispose(): void;
    }
}
