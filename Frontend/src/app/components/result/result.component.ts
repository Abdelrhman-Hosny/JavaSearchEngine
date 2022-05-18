import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {SearchService} from "../../search.service";
import {Router} from "@angular/router";
import { ResponseObject } from 'src/app/ResponesObject';

@Component({
  selector: 'app-result',
  templateUrl: './result.component.html',
  styleUrls: ['./result.component.scss']
})
export class ResultComponent implements OnInit,OnDestroy {
  @Input() value :string | any;
  
  term: any;
  totalLength:any;
  page:number=1;
  results: Array<ResponseObject> = [];
  myR: ResponseObject = new ResponseObject;

  constructor(private searchService:SearchService,private router:Router) { 
   }

  ngOnInit(): void {
    const {term} = history.state;
    this.term = term;
    for(let i = 0 ; i < 20 ; i++ ){
      this.results.push(this.myR);
      this.totalLength = this.results.length;
    }
    // TODO :: PUT HTTP HERE ALSO
    // if (term) {
    //   this.subs.push
    //   (this.searchService.getSearchData(term).subscribe((data: String) => {
    //       this.results = data;
    //       // this.totalLength = this.results?.items?.length;
    //   })
    //   )
    // }
  }

  ngOnDestroy():void {
    
  }
  
  search():void
  {
      this.term=this.value
      
      this.searchService.getSearchData(this.value).subscribe((data: String) => {
          
          //TODO:: put data here after http request
          // this.results = data;
          console.log(this.results);
          // this.totalLength = this.results?.items?.length
          this.page =1;
        })
      
    }
  
    acceptdata(data:any)
  {
    this.value=data;
  }

}
