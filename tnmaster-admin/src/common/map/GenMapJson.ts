interface Feature {
  type: 'Feature'
  properties: {
    adcode: number
    name: string
    center: [number, number]
    centerid: [number, number]
    childrenNum: number
    level: 'province' | 'city' | 'district' | 'street' | 'town' | 'village'
    parent: { adcode: number }
    subFeatureIndex: number
    acroutes: [number]
    adchar?: undefined
    geometry: {
      type: 'MultiPolygon'
      coordinates: [number[][][]]
    }
  }
}

export interface GenMapJson {
  type: 'FeatureCollection'
  features: Feature[]
}
