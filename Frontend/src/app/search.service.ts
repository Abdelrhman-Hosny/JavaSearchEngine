import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../environments/environment";
import { Observable} from "rxjs";
import{map} from 'rxjs/operators'
import { AutoCompleteObject } from './AutoCompleteObject';
import { ResponseObjects } from './ResponesObjects';



@Injectable({
  providedIn: 'root'
})
export class SearchService {
  Search_url:string="http://localhost:8080/search";
  AutoComplete_url:string="http://localhost:8080/auto-complete";

  // put them in env

  
  constructor(private httpClient:HttpClient) {
  }
  getSearchData(searchTerm:string):Observable<ResponseObjects>
  {
    const res = this.httpClient.get<ResponseObjects>(this.Search_url+"&query=" + searchTerm);
    return res; 
  }

  getAutoCompleteData(searchTerm:string):Observable<AutoCompleteObject>
  {
    
    const res = this.httpClient.get<AutoCompleteObject>(this.AutoComplete_url+"&query=" + searchTerm);
    return res; 
  }
}
