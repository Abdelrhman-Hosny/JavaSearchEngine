export interface GoogleResponse {
  kind: string;
  url: Url;
  queries: Queries;
  context: Context;
  searchInformation: SearchInformation;
  items: (ItemsEntity)[] | null;
}
export interface Url {
  type: string;
  template: string;
}
export interface Queries {
  request?: (RequestEntityOrNextPageEntity)[] | null;
  nextPage?: (RequestEntityOrNextPageEntity)[] | null;
}
export interface RequestEntityOrNextPageEntity {
  title: string;
  totalResults: string;
  searchTerms: string;
  count: number;
  startIndex: number;
  inputEncoding: string;
  outputEncoding: string;
  safe: string;
  cx: string;
}
export interface Context {
  title: string;
  facets?: ((EntityOrFacetsEntityEntity)[] | null)[] | null;
}
export interface EntityOrFacetsEntityEntity {
  anchor: string;
  label: string;
  label_with_op: string;
}
export interface SearchInformation {
  searchTime: number;
  formattedSearchTime: string;
  totalResults: string;
  formattedTotalResults: string;
}
export interface ItemsEntity {
  kind: string;
  title: string;
  htmlTitle: string;
  link: string;
  displayLink: string;
  snippet: string;
  htmlSnippet: string;
  cacheId: string;
  formattedUrl: string;
  htmlFormattedUrl: string;
  pagemap?: Pagemap | null;
  mime?: string | null;
  fileFormat?: string | null;
  labels?: (LabelsEntity)[] | null;
}
export interface Pagemap {
  cse_thumbnail?: (CseThumbnailEntity)[] | null;
  metatags?: (MetatagsEntity)[] | null;
  cse_image?: (CseImageEntity)[] | null;
}
export interface CseThumbnailEntity {
  src: string;
  width: string;
  height: string;
}
export interface MetatagsEntity {
  moddate: string;
  creator: string;
  creationdate: string;
  author: string;
  producer: string;
}
export interface CseImageEntity {
  src: string;
}
export interface LabelsEntity {
  name: string;
  displayName: string;
  label_with_op: string;
}
