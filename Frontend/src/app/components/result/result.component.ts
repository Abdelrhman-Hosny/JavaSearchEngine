import {Component, OnDestroy, OnInit} from '@angular/core';
import {GoogleResponse} from "../../GoogleResponse.model";
import {Subscription} from "rxjs";
import {SearchService} from "../../search.service";
import {NgForm} from "@angular/forms";
import {Router} from "@angular/router";

@Component({
  selector: 'app-result',
  templateUrl: './result.component.html',
  styleUrls: ['./result.component.scss']
})
export class ResultComponent implements OnInit,OnDestroy {

  subs:Subscription[]=[];
  term: any;
  totalLength:any;
  page:number=1;
  results: any ;


  constructor(private searchService:SearchService,private router:Router) { }

  ngOnInit(): void {
    const {term} = history.state;
    this.term = term;
    if (term) {
      console.log("term"+ term);
      this.subs.push
      (this.searchService.getSearchData(term).subscribe((data: GoogleResponse) => {
          this.results = data;
          this.totalLength = this.results?.items?.length;
      })
      )
    }

  }
  ngOnDestroy():void {
  this.subs.map(s=>s.unsubscribe());
}
  search(form:NgForm):void
  {
      const{search_term}=form.value;
      this.term=search_term
      this.subs.push
      (this.searchService.getSearchData(search_term).subscribe((data: GoogleResponse) => {
          this.results = data;
          this.totalLength = this.results?.items?.length
          this.page =1;
        })
      )
    }

}
