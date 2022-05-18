import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {SearchService} from "../../search.service";
import {Router} from "@angular/router";
import { ResponseObjects,ResponseObject } from 'src/app/ResponesObjects';

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
  results: ResponseObject[] = [];
  

  constructor(private searchService:SearchService,private router:Router) { 
   }

  ngOnInit(): void {
    const {term} = history.state;
    this.term = term;
    
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
      
      this.searchService.getSearchData(this.value).subscribe((data: ResponseObjects) => {
          // assigning results to array of ResponseObject got from backend
          this.results = data.list;          
          this.totalLength = this.results.length
          this.page =1;
        })
      
    }
  
    acceptdata(data:any)
  {
    this.value=data;
  }

}
