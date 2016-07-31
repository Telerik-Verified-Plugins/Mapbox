/// <reference path="../../libs/typings/tsd.d.ts" />
declare var Mapbox: mapbox.IMap;

declare module mapbox {
    export interface IMarkerOptions {
        lat: number,
        lng: number,
        /**no popup unless set*/
        title?: string,
        subtitle?: string,
        image?: string | {
            url?: string,
            data?: string,
            svg?: string,
            height?: number,
            width?: number
        }
    }

    export interface IGeoPoint {
        lat: number,
        lng: number
    }

    export interface ITilt {
        pitch: number,
        duration: number
    }

    export interface IMap {
        show: (options: {
            /** light|dark|emerald|satellite|streets , default 'streets'*/
            style: string,
            /***/
            margins?: {
                left?: number, // default 0
                right?: number, // default 0
                top?: number, // default 0
                bottom?: number // default 0
            },
            center?: { // optional, without a default
                lat: number,
                lng: number
            },
            /**0 (the entire world) to 20, default 10*/
            zoomLevel?: number,
            /**your app will ask permission to the user, default false*/
            showUserLocation?: boolean,
            /**default false, Mapbox requires this default if you're on a free plan*/
            hideAttribution: boolean,
            /** default false, Mapbox requires this default if you're on a free plan*/
            hideLogo: boolean,
            hideCompass?: boolean, // default false
            disableRotation?: boolean, // default false
            disableScroll?: boolean, // default false
            disableZoom?: boolean, // default false
            /** disable the two-finger perspective gesture, default false*/
            disablePitch?: boolean,
            markers?: IMarkerOptions[]
        }, successCallback?: (msg: string) => void,
            errorCallback?: (msg: string) => void) => void,
        hide: (options?: any,
            successCallback?: (msg: string) => void,
            errorCallback?: (msg: string) => void) => void,
        addMarkerCallback: (callback: (selectedMarker: IMarkerOptions) => void) => void,
        getCenter: (callback: (p: IGeoPoint) => void) => void,
        setCenter: (p: IGeoPoint) => void,
        getZoomLevel: (callback: (z: number) => void) => void,
        getTilt: (callback: (z: ITilt) => void) => void,
        setTilt: (p: ITilt) => void,
    }
}
