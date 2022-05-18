import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../environments/environment";
import {GoogleResponse} from "./GoogleResponse.model";
import { Observable} from "rxjs";
import{map} from 'rxjs/operators'


@Injectable({
  providedIn: 'root'
})
export class SearchService {
  url:string=environment.Server_URL;
  api_key:string=environment.API_KEY;
  cx_key:string=environment.CX_KEY



  constructor(private httpClient:HttpClient) {


  }
  getSearchData(searchTerm:string):Observable<GoogleResponse>
  {
    return this.httpClient.get<GoogleResponse>(this.url,{
      params:{
        key:this.api_key,
        cx:this.cx_key,
        q:searchTerm


      }


    });

  }
}
