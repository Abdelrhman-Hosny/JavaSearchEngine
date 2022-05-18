import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {GoogleResponse} from "../../GoogleResponse.model";
import {Subscription} from "rxjs";
import {SearchService} from "../../search.service";
import {FormControl, NgForm} from "@angular/forms";
import {Router} from "@angular/router";

@Component({
  selector: 'app-result',
  templateUrl: './result.component.html',
  styleUrls: ['./result.component.scss']
})
export class ResultComponent implements OnInit,OnDestroy {
  @Input() value :string | any;
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
  search():void
  {
    this.term=this.value
      this.subs.push
      (this.searchService.getSearchData(this.value).subscribe((data: GoogleResponse) => {
          this.results = data;
          this.totalLength = this.results?.items?.length
          this.page =1;
        })
      )
    }
  acceptdata(data:any)
  {
    this.value=data;
  }

}
